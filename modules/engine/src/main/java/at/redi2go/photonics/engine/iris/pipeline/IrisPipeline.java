package at.redi2go.photonics.engine.iris.pipeline;

import at.redi2go.photonics.engine.iris.pipeline.buffer.IBufferHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.texture.ISamplerHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.pipeline.uniform.IDynamicUniformHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.uniform.IUniformHolderBuilder;
import at.redi2go.photonics.engine.rendering.RenderingComponent;

public interface IrisPipeline extends
        RenderingComponent,
        IBufferHolderBuilder<IrisPipeline>,
        ISamplerHolderBuilder<IrisPipeline>,
        IDynamicUniformHolderBuilder<IrisPipeline>,
        IUniformHolderBuilder<IrisPipeline> {
    IrisFramebuffer.Builder newFramebuffer(int width, int height);

    IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale);

    default IrisFramebuffer.Builder newFramebuffer(float scale) {
        return newFramebuffer(scale, scale);
    }

    IrisRenderer.Builder newRenderer();
}
