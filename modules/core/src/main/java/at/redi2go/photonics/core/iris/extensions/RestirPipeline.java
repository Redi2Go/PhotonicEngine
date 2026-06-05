package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_PREV_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class RestirPipeline extends AbstractPhotonicsExtension {
    private final IrisFramebuffer historyFramebuffer;
    private final IrisRenderer historyRenderer;

    private final IrisFramebuffer restirFramebuffer;
    private final IrisRenderer restirRenderer;

    private int atrousIteration = 0;
    private final int denoiserPasses;

    private final IrisFramebuffer denoiseFramebuffer;
    private final IrisRenderer denoiseRenderer;

    private final IrisFramebuffer otherFramebuffer;
    private final IrisRenderer otherRenderer;

    private final UniformUpdater atrousUpdater = new UniformUpdater();

    public RestirPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipelineFactory passFactory
    ) {
        super(properties, atlasDownloader, handheldItemSupplier);

        this.historyFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("restir_position_history", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_normal_history", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .build());

        this.historyRenderer = passFactory.newRenderer("history")
                .addPass("record history", "/photonics/rendering/restir/passes/r0_record_history.fsh", null, historyFramebuffer)
                .build();
        

        this.restirFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("restir_lighting", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirEnabled)
                .addAttachment("restir_lighting_variance", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirEnabled)
                .addAttachment("restir_lighting_samples", ITextureFormat.r16f(), CREATE_SAMPLER, this::isRestirEnabled)
                .addAttachment("restir_direct_reservoirs0", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isBlockLightEnabled)
                .addAttachment("restir_indirect_reservoirs0", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirGiEnabled)
                .addAttachment("restir_indirect_reservoirs1", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirGiEnabled)
                .addAttachment("restir_indirect_reservoirs2", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirGiEnabled)
                .build());

        this.restirRenderer = passFactory.newRenderer("restir")
                .addPass("initial direct", "/photonics/rendering/restir/passes/r1_initial_direct.fsh", null, restirFramebuffer, this::isBlockLightEnabled)
                .addPass("initial indirect", "/photonics/rendering/restir/passes/r2_initial_indirect.fsh", null, restirFramebuffer, this::isRestirGiEnabled)
                .addPass("temporal reuse", "/photonics/rendering/restir/passes/r3_temporal_reuse.fsh", null, restirFramebuffer, this::isRestirEnabled)
                .addPass("spatial reuse (setup)", spatialReusePass("setup.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("spatial reuse #1", spatialReusePass("pass0.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("spatial reuse #2", spatialReusePass("pass1.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("spatial reuse #3", spatialReusePass("pass2.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("diffuse", "/photonics/rendering/restir/passes/r5_diffuse.fsh", null, restirFramebuffer, this::isRestirEnabled)
                .addPass("accumulation", "/photonics/rendering/restir/passes/r6_accumulation.fsh", null, restirFramebuffer, this::isRestirEnabled)
                .build();


        // The hand always needs at least 7 denoiser passes.
        int requestedDenoiserPasses = properties.getRestirDenoiserPasses();
        this.denoiserPasses = requestedDenoiserPasses != 0 ? Math.max(requestedDenoiserPasses, 7) : 0;

        this.denoiseFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("denoise_color", ITextureFormat.rgb16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isDenoisingEnabled)
                .addAttachment("denoise_variance", ITextureFormat.r16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isDenoisingEnabled)
                .build());

        this.denoiseRenderer = passFactory.newRenderer("denoiser")
                .addPass("denoise", "/photonics/rendering/restir/passes/r7_denoising.fsh", null, denoiseFramebuffer, this::isDenoisingEnabled)
                .build();


        this.otherFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("other_handheld", ITextureFormat.rgb16f(), CREATE_SAMPLER, this::isHandheldLightingEnabled)
                .build());

        this.otherRenderer = passFactory.newRenderer("other")
                .addPass("handheld", "/photonics/rendering/restir/passes/r8_handheld.fsh", null, otherFramebuffer, this::isHandheldLightingEnabled)
                .build();
    }

    @Override
    public void onRender() {
        historyFramebuffer.flip();
        historyRenderer.renderAll();

        restirFramebuffer.flip();
        restirRenderer.renderAll();

        for (atrousIteration = -1; atrousIteration < denoiserPasses; atrousIteration++) {
            atrousUpdater.updateNow();

            denoiseFramebuffer.flip();
            denoiseRenderer.renderAll();
        }

        otherRenderer.renderAll();
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        super.registerDynamicUniforms(dynamicUniforms);

        dynamicUniforms.uniform1i(
                "atrous_iteration",
                () -> atrousIteration,
                atrousUpdater.newNotifier()
        );
    }

    public boolean isBlockLightEnabled() {
        return properties.isBlockLightEnabled();
    }

    public boolean isRestirGiEnabled() {
        return properties.isGiEnabled() && properties.useRestirCombinedGi();
    }

    public boolean isRestirEnabled() {
        return isBlockLightEnabled() || isRestirGiEnabled();
    }

    public boolean isSpatialReuseEnabled() {
        return properties.getRestirSpatialReuseSamples() > 0;
    }

    public boolean isHandheldLightingEnabled() {
        return properties.isHandheldLightEnabled();
    }

    public boolean isDenoisingEnabled() {
        return isRestirEnabled() && denoiserPasses > 0;
    }

    private String spatialReusePass(String file) {
        return "/photonics/rendering/restir/passes/spatial_reuse/" + file;
    }
}
