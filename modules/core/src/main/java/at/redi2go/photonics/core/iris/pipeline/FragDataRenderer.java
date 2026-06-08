package at.redi2go.photonics.core.iris.pipeline;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.rendering.AbstractRenderingComponent;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_PREV_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class FragDataRenderer extends AbstractRenderingComponent implements IrisRenderer, Disposable {
    private final IrisFramebuffer fragFramebuffer;
    private final IrisRenderer fragRenderer;

    public FragDataRenderer(IrisPipelineFactory passFactory, float renderScale) {
       this.fragFramebuffer = registerComponent(passFactory.newFramebuffer(renderScale)
               .addAttachment("ph_frag_data0", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
               .addAttachment("ph_frag_data1", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER)
               .build());

        this.fragRenderer = passFactory.newRenderer("frag data")
                .addPass("frag data", "/photonics/rendering/frag/f0_load_frag.fsh", null, fragFramebuffer)
                .build();
    }

    @Override
    public void renderAll() {
        fragFramebuffer.flip();
        fragRenderer.renderAll();
    }
}
