package at.redi2go.photonics.engine.iris.pipeline;

import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.textures.ISamplerHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolderBuilder;
import at.redi2go.photonics.engine.rendering.RenderingComponent;

public interface IrisPipeline extends
        RenderingComponent,
        IrisDefineHolderBuilder<IrisPipeline>,
        IrisBufferHolderBuilder<IrisPipeline>,
        ISamplerHolderBuilder<IrisPipeline>,
        IrisDynamicUniformHolderBuilder<IrisPipeline>,
        IrisUniformHolderBuilder<IrisPipeline> {
    IrisFramebuffer.Builder newFramebuffer(int width, int height);

    IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale);

    default IrisFramebuffer.Builder newFramebuffer(float scale) {
        return newFramebuffer(scale, scale);
    }

    IrisRenderer.Builder newRenderer();
}
