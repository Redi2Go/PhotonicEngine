package at.redi2go.photonics.core.iris.pipeline.rendering;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;

public interface IrisPipelineFactory {
    IrisFramebuffer.Builder newFramebuffer(int width, int height);

    IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale);

    default IrisFramebuffer.Builder newFramebuffer(float scale) {
        return newFramebuffer(scale, scale);
    }

    IrisRenderer.Builder newRenderer(String name);
}
