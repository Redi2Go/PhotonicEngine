package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisFactory;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_PREV_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class Pipelines {
    private Pipelines() {

    }

    public static void fragData(AbstractPhotonicsExtension ext, IrisFactory irisFactory, float renderScale) {
        var framebuffer = irisFactory.newFramebuffer(renderScale)
                .addAttachment("ph_frag_data0", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .addAttachment("ph_frag_data1", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
                .build(ext::registerComponent);

        irisFactory.newPipeline()
                .debugGroup("frag data")
                .thenFlip(framebuffer)
                .deferredPass("frag data", framebuffer, "/photonics/rendering/frag/f0_load_frag.fsh", null)
                .build(ext::registerRenderer);
    }
}
