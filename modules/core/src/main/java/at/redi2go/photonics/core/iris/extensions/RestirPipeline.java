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

    private final @Nullable IrisFramebuffer denoiseFramebuffer;
    private final @Nullable IrisRenderer denoiseRenderer;

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
                .addAttachment("restir_lighting", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting_variance", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting_samples", ITextureFormat.r16f(), CREATE_SAMPLER)
                .addAttachment("restir_direct_reservoirs0", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_indirect_reservoirs0", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_indirect_reservoirs1", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("restir_indirect_reservoirs2", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .build());

        var restirBuilder = passFactory.newRenderer("restir");

        restirBuilder.addPass("initial direct", "/photonics/rendering/restir/passes/r1_initial_direct.fsh", null, restirFramebuffer);
        restirBuilder.addPass("initial indirect", "/photonics/rendering/restir/passes/r2_initial_indirect.fsh", null, restirFramebuffer);
        restirBuilder.addPass("temporal reuse", "/photonics/rendering/restir/passes/r3_temporal_reuse.fsh", null, restirFramebuffer);

        if (properties.getRestirSpatialReuseSamples() != 0) {
            restirBuilder.addPass("spatial reuse (setup)", spatialReusePass("setup.fsh"), null, restirFramebuffer);
            restirBuilder.addPass("spatial reuse #1", spatialReusePass("pass0.fsh"), null, restirFramebuffer);
            restirBuilder.addPass("spatial reuse #2", spatialReusePass("pass1.fsh"), null, restirFramebuffer);
            restirBuilder.addPass("spatial reuse #3", spatialReusePass("pass2.fsh"), null, restirFramebuffer);
        }

        restirBuilder.addPass("diffuse", "/photonics/rendering/restir/passes/r5_diffuse.fsh", null, restirFramebuffer);
        restirBuilder.addPass("accumulation", "/photonics/rendering/restir/passes/r6_accumulation.fsh", null, restirFramebuffer);


        this.restirRenderer = restirBuilder.build();

        // The hand always needs at least 7 denoiser passes.
        int requestedDenoiserPasses = properties.getRestirDenoiserPasses();
        if (requestedDenoiserPasses != 0) {
            denoiserPasses = Math.max(requestedDenoiserPasses, 7);
        } else denoiserPasses = 0;

        if (denoiserPasses != 0) {
            this.denoiseFramebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                    .addAttachment("denoise_color", ITextureFormat.rgb16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                    .addAttachment("denoise_variance", ITextureFormat.r16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                    .build());

            this.denoiseRenderer = passFactory.newRenderer("denoiser")
                    .addPass("denoise", "/photonics/rendering/restir/passes/r7_denoising.fsh", null, denoiseFramebuffer)
                    .build();
        } else {
            this.denoiseFramebuffer = null;
            this.denoiseRenderer = null;
        }
    }

    @Override
    public void onRender() {
        historyFramebuffer.flip();
        historyRenderer.renderAll();

        restirFramebuffer.flip();
        restirRenderer.renderAll();

        if (denoiseFramebuffer != null && denoiseRenderer != null) {
            for (atrousIteration = -1; atrousIteration < denoiserPasses; atrousIteration++) {
                atrousUpdater.updateNow();

                denoiseFramebuffer.flip();
                denoiseRenderer.renderAll();
            }
        }
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        super.registerDynamicUniforms(dynamicUniforms);

        if (denoiserPasses != 0) {
            dynamicUniforms.uniform1i(
                    "atrous_iteration",
                    () -> atrousIteration,
                    atrousUpdater.newNotifier()
            );
        }
    }

    private @Nullable String spatialReusePass(String file) {
        return properties.getRestirSpatialReuseSamples() < 0 ? null : "/photonics/rendering/restir/passes/spatial_reuse/" + file;
    }
}
