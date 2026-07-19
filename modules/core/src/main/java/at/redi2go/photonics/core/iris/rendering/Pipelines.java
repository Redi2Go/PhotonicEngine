package at.redi2go.photonics.core.iris.rendering;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.core.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.core.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.core.rendering.HandheldLightComponent;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class Pipelines {
    public static String DEFAULT_VERTEX_SHADER = "/photonics/rendering/shared/screen.vsh";

    private Pipelines() {

    }

    public static void fragData(PhotonicsPipeline ext, PhotonicsProperties properties, IrisPipeline irisPipeline) {
        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("frag_data0", ITextureFormat.rgba32f(), CREATE_SAMPLER | FLIP)
                .addAttachment("frag_data1", ITextureFormat.rgba32ui(), CREATE_SAMPLER | FLIP)
                .addAttachment("fast_frag_data", ITextureFormat.rg32f(), CREATE_SAMPLER | FLIP)
                .build(ext::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("frag data")
                .withFragmentPrefix("/photonics/rendering/frag/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("frag data", "f0_load_frag.fsh", null)
                .build(ext::registerRenderer);
    }

    public static void handheldLighting(
            PhotonicsPipeline ext,
            HandheldItemSupplier handheldItemSupplier,
            PhotonicsProperties properties,
            IrisPipeline irisPipeline
    ) {
        if (!properties.isHandheldLightEnabled()) return;

        var handheldComponent = ext.registerComponent(new HandheldLightComponent(handheldItemSupplier, properties));

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("handheld_diffuse", ITextureFormat.rgb32f(), CREATE_SAMPLER)
                .build(ext::registerComponent);

        var pipeline = irisPipeline.newRenderer()
                .debugGroup("handheld")
                .withFragmentPrefix("/photonics/rendering/shared/")
                .withFramebuffer(framebuffer)
                .deferredPass("handheld", "h0_handheld.fsh", null)
                .build();

        ext.registerRenderer(() -> {
            if (handheldComponent.hasItem())
                pipeline.renderAll();
        });
    }

    public static void exposureHistory(PhotonicsPipeline ext, IrisPipeline irisPipeline) {
        var framebuffer = irisPipeline.newFramebuffer(1, 1)
                .addAttachment("prev_exposure", ITextureFormat.r32f(), CREATE_SAMPLER)
                .build(ext::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("exposure")
                .withFragmentPrefix("/photonics/rendering/frag/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("record exposure", "e0_record_exposure.fsh", null)
                .build(ext::registerRenderer);
    }
}
