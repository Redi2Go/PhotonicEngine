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
import org.jetbrains.annotations.Nullable;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_PREV_SAMPLER;
import static java.lang.Math.max;

public class RestirDiPipeline extends AbstractPhotonicsExtension {
    private final IrisFramebuffer restirFramebuffer;
    private final IrisRenderer restirRenderer;

    private int atrousIteration = 0;
    private final int denoiserPasses;

    private final IrisFramebuffer denoiseFramebuffer;
    private final IrisRenderer denoiseRenderer;

    private final UniformUpdater atrousUpdater = new UniformUpdater();

    public RestirDiPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipelineFactory passFactory
    ) {
        super(properties, atlasDownloader, handheldItemSupplier);

        this.restirFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("restir_position_history", ITextureFormat.rgb16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_normal_history", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_reservoirs", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting_variance", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting_samples", ITextureFormat.r16f(), CREATE_SAMPLER)
                .addAttachment("restir_handheld", ITextureFormat.rgb16f(), CREATE_SAMPLER, this::isHandheldLightingEnabled)
                .build());

        this.restirRenderer = passFactory.newRenderer("restir")
                .addPass("initial sampling", "/photonics/rendering/restir_di/passes/sampling.fsh", null, restirFramebuffer, this::isBlockLightEnabled)
                .addPass("spatial reuse (setup)", spatialReusePass("setup.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("spatial reuse #1", spatialReusePass("pass0.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("spatial reuse #2", spatialReusePass("pass1.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("spatial reuse #3", spatialReusePass("pass2.fsh"), null, restirFramebuffer, this::isSpatialReuseEnabled)
                .addPass("direct", "/photonics/rendering/restir_di/passes/direct.fsh", null, restirFramebuffer, this::isBlockLightEnabled)
                .addPass("indirect", "/photonics/rendering/restir_di/passes/combined_indirect.fsh", null, restirFramebuffer, this::isCombinedGiEnabled)
                .addPass("handheld", "/photonics/rendering/restir_di/passes/handheld.fsh", null, restirFramebuffer, this::isHandheldLightingEnabled)
                .addPass("accumulation", "/photonics/rendering/restir_di/passes/accumulation.fsh", null, restirFramebuffer, this::isAccumulationEnabled)
                .build();

        int requestedDenoiserPasses = properties.getRestirDenoiserPasses();
        this.denoiserPasses = requestedDenoiserPasses > 0 ? max(requestedDenoiserPasses, 7) : 0;
        
        this.denoiseFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("denoise_color", ITextureFormat.rgb16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isDenoisingEnabled)
                .addAttachment("denoise_variance", ITextureFormat.r16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isDenoisingEnabled)
                .build());

        this.denoiseRenderer = passFactory.newRenderer("denoiser")
                .addPass("denoise", "/photonics/rendering/restir_di/passes/denoising.fsh", null, denoiseFramebuffer, this::isDenoisingEnabled)
                .build();
    }

    @Override
    public void onRender() {
        restirFramebuffer.flip();
        restirRenderer.renderAll();

        for (atrousIteration = -1; atrousIteration < denoiserPasses; atrousIteration++) {
            atrousUpdater.updateNow();

            denoiseFramebuffer.flip();
            denoiseRenderer.renderAll();
        }
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

    public boolean isSpatialReuseEnabled() {
        return properties.getRestirSpatialReuseSamples() > 0;
    }

    public boolean isHandheldLightingEnabled() {
        return properties.isHandheldLightEnabled();
    }

    public boolean isCombinedGiEnabled() {
        return properties.isGiEnabled() && properties.useRestirCombinedGi();
    }

    public boolean isAccumulationEnabled() {
        return properties.isBlockLightEnabled() || properties.useRestirCombinedGi();
    }

    public boolean isDenoisingEnabled() {
        return isAccumulationEnabled() && denoiserPasses > 0;
    }

    private String spatialReusePass(String file) {
        return "/photonics/rendering/restir_di/passes/spatial_reuse/" + file;
    }
}
