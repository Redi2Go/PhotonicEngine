package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

public class RestirDiPipeline extends AbstractPhotonicsExtension {
    private final IrisFramebuffer framebuffer;
    private final IrisRenderer renderer;

    public RestirDiPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            IrisPipelineFactory passFactory
    ) {
        super(properties, atlasDownloader);

        this.framebuffer = registerComponent(passFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("restir_position_history", ITextureFormat.rgb16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                .addAttachment("restir_normal_history", ITextureFormat.rgba16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                .addAttachment("restir_reservoirs", ITextureFormat.rgba32f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting", ITextureFormat.rgba32f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting_variance", ITextureFormat.rgba32f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                .addAttachment("restir_lighting_samples", ITextureFormat.r16f(), AttachmentUsage.FLIP | AttachmentUsage.CREATE_SAMPLER | AttachmentUsage.CREATE_PREV_SAMPLER)
                .build());

        this.renderer = passFactory.newRenderer("restir")
                .addPass("initial sampling", "/photonics/rendering/restir_di/passes/sampling.fsh", null, framebuffer)
                .addPass("spatial reuse (setup)", "/photonics/rendering/restir_di/passes/spatial_reuse/setup.fsh", null, framebuffer)
                .addPass("spatial reuse #1", "/photonics/rendering/restir_di/passes/spatial_reuse/pass0.fsh", null, framebuffer)
                .addPass("spatial reuse #2", "/photonics/rendering/restir_di/passes/spatial_reuse/pass1.fsh", null, framebuffer)
                .addPass("spatial reuse #3", "/photonics/rendering/restir_di/passes/spatial_reuse/pass2.fsh", null, framebuffer)
                .addPass("lighting", "/photonics/rendering/restir_di/passes/lighting.fsh", null, framebuffer)
                .addPass("accumulation", "/photonics/rendering/restir_di/passes/accumulation.fsh", null, framebuffer)
                .build();
    }

    @Override
    public void onRender() {
        framebuffer.flip();
        renderer.renderAll();
    }
}
