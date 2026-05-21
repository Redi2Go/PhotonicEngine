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

public class RestirDiPipeline extends AbstractPhotonicsExtension {
    private final IrisFramebuffer restirFramebuffer;
    private final IrisRenderer restirRenderer;

    private int atrousIteration = 0;
    private final int denoiserPasses;

    private final @Nullable IrisFramebuffer denoiseFramebuffer;
    private final @Nullable IrisRenderer denoiseRenderer;

    private final UniformUpdater atrousUpdater = new UniformUpdater();

    public RestirDiPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipelineFactory passFactory
    ) {
        super(properties, atlasDownloader, handheldItemSupplier);

        var restirFramebufferBuilder = passFactory.newFramebuffer(properties.getRenderScale());

        restirFramebufferBuilder.addAttachment("restir_position_history", ITextureFormat.rgb16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER);
        restirFramebufferBuilder.addAttachment("restir_normal_history", ITextureFormat.rgba16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER);
        restirFramebufferBuilder.addAttachment("restir_reservoirs", ITextureFormat.rgba32f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER);
        restirFramebufferBuilder.addAttachment("restir_lighting", ITextureFormat.rgba32f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER);
        restirFramebufferBuilder.addAttachment("restir_lighting_variance", ITextureFormat.rgba16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER);
        restirFramebufferBuilder.addAttachment("restir_lighting_samples", ITextureFormat.r16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER);

        if (properties.isHandheldLightEnabled()) {
            restirFramebufferBuilder.addAttachment("restir_handheld", ITextureFormat.rgb16f(), AttachmentUsage.CREATE_SAMPLER);
        }

        this.restirFramebuffer = registerComponent(restirFramebufferBuilder.build());




        var restirBuilder = passFactory.newRenderer("restir");

        restirBuilder.addPass("initial sampling", "/photonics/rendering/restir_di/passes/sampling.fsh", null, restirFramebuffer);

        if (properties.getRestirSpatialReuseSamples() != 0) {
            restirBuilder.addPass("spatial reuse (setup)", spatialReusePass("setup.fsh"), null, restirFramebuffer);
            restirBuilder.addPass("spatial reuse #1", spatialReusePass("pass0.fsh"), null, restirFramebuffer);
            restirBuilder.addPass("spatial reuse #2", spatialReusePass("pass1.fsh"), null, restirFramebuffer);
            restirBuilder.addPass("spatial reuse #3", spatialReusePass("pass2.fsh"), null, restirFramebuffer);
        }

        restirBuilder.addPass("direct", "/photonics/rendering/restir_di/passes/direct.fsh", null, restirFramebuffer);

        if (properties.useRestirCombinedGi()) {
            restirBuilder.addPass("indirect", "/photonics/rendering/restir_di/passes/combined_indirect.fsh", null, restirFramebuffer);
        }

        if (properties.isHandheldLightEnabled()) {
            restirBuilder.addPass("handheld", "/photonics/rendering/restir_di/passes/handheld.fsh", null, restirFramebuffer);
        }

        restirBuilder.addPass("accumulation", "/photonics/rendering/restir_di/passes/accumulation.fsh", null, restirFramebuffer);

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
                    .addPass("denoise", "/photonics/rendering/restir_di/passes/denoising.fsh", null, denoiseFramebuffer)
                    .build();
        } else {
            this.denoiseFramebuffer = null;
            this.denoiseRenderer = null;
        }
    }

    @Override
    public void onRender() {
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
        return properties.getRestirSpatialReuseSamples() < 0 ? null : "/photonics/rendering/restir_di/passes/spatial_reuse/" + file;
    }
}
