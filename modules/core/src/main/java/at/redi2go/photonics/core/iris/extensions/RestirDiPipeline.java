package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import org.jetbrains.annotations.Nullable;

public class RestirDiPipeline extends AbstractPhotonicsExtension {
    private final IrisFramebuffer framebuffer;
    private final IrisRenderer renderer;

    public RestirDiPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            IrisPipelineFactory passFactory
    ) {
        super(properties, atlasDownloader);

        this.framebuffer = registerComponent(passFactory.newFramebuffer(1f)
                .addAttachment("restir_reservoirs", ITextureFormat.rgba32f(), AttachmentUsage.CREATE_SAMPLER)
                .addAttachment("restir_direct", ITextureFormat.rgb32f(), AttachmentUsage.CREATE_SAMPLER)
                .build());

        this.renderer = passFactory.newRenderer("restir")
                .addPass("initial sampling", "/photonics/rendering/restir_di/passes/sampling.fsh", null, framebuffer)
                .addPass("lighting", "/photonics/rendering/restir_di/passes/lighting.fsh", null, framebuffer)
                .build();
    }

    @Override
    public void onRender() {
        renderer.renderAll();
    }
}
