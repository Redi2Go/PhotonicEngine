package at.redi2go.photonics.engine.iris.rendering;

import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.engine.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
//import at.redi2go.photonics.core.rendering.HandheldLightComponent;

import static at.redi2go.photonics.engine.iris.pipeline.textures.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.engine.iris.pipeline.textures.AttachmentUsage.FLIP;

public class Pipelines {
    public static String DEFAULT_VERTEX_SHADER = "/photonics/rendering/shared/screen.vsh";

    private Pipelines() {

    }

    public static void fragData(PhotonicsPipeline ext, PhotonicsProperties properties, IrisPipeline irisPipeline) {
        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale(), ext::registerComponent)
                .addAttachment("frag_data0", TextureFormat.RGBA32F, CREATE_SAMPLER | FLIP)
                .addAttachment("frag_data1", TextureFormat.RGBA32UI, CREATE_SAMPLER | FLIP)
                .addAttachment("fast_frag_data", TextureFormat.RG32F, CREATE_SAMPLER | FLIP)
                .build();

        irisPipeline.newRenderer(ext::registerRenderer)
                .debugGroup("frag data")
                .withFragmentPrefix("/photonics/rendering/frag/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("frag data", "f0_load_frag.fsh", null)
                .build();
    }

    public static void handheldLighting(
            PhotonicsPipeline ext,
            PhotonicsProperties properties,
            IrisPipeline irisPipeline
    ) {
        if (!properties.getHandheldProperties().isEnabled()) return;

//        var handheldComponent = ext.registerComponent(new HandheldLightComponent(handheldItemSupplier, properties));

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale(), ext::registerComponent)
                .addAttachment("handheld_diffuse", TextureFormat.RGB32F, CREATE_SAMPLER)
                .build();

        var pipeline = irisPipeline.newRenderer(ext::registerRenderer)
                .debugGroup("handheld")
                .withFragmentPrefix("/photonics/rendering/shared/")
                .withFramebuffer(framebuffer)
                .deferredPass("handheld", "h0_handheld.fsh", null)
                .build();

//        ext.registerRenderer(() -> {
//            if (handheldComponent.hasItem())
//                pipeline.renderAll();
//        });
    }

    public static void exposureHistory(PhotonicsPipeline ext, IrisPipeline irisPipeline) {
        var framebuffer = irisPipeline.newFramebuffer(1, 1, ext::registerComponent)
                .addAttachment("prev_exposure", TextureFormat.R32F, CREATE_SAMPLER)
                .build();

        irisPipeline.newRenderer(ext::registerRenderer)
                .debugGroup("exposure")
                .withFragmentPrefix("/photonics/rendering/frag/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("record exposure", "e0_record_exposure.fsh", null)
                .build();
    }
}
