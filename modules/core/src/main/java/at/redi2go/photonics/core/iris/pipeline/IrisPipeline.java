package at.redi2go.photonics.core.iris.pipeline;

import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.rendering.RenderingComponent;

public interface IrisPipeline extends RenderingComponent, IDynamicUniformHolder, IBufferHolder, ISamplerHolder {
    IrisFramebuffer.Builder newFramebuffer(int width, int height);

    IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale);

    default IrisFramebuffer.Builder newFramebuffer(float scale) {
        return newFramebuffer(scale, scale);
    }

    IrisRenderer.Builder newRenderer();
}
