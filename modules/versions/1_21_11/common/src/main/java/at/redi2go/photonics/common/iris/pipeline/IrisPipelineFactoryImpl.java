package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.framebuffer.FramebufferSize;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.IrisFramebufferBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererImpl;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipelineFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;

import java.util.List;

public class IrisPipelineFactoryImpl implements IrisPipelineFactory {
    private final List<IrisRendererImpl> commonRenderers;

    public IrisPipelineFactoryImpl(List<IrisRendererImpl> commonRenderers) {
        this.commonRenderers = commonRenderers;
    }

    @Override
    public IrisFramebuffer.Builder newFramebuffer(int width, int height) {
        return new IrisFramebufferBuilderImpl(
                new FramebufferSize.Fixed(width, height)
        );
    }

    @Override
    public IrisFramebuffer.Builder newFramebuffer(float widthScale, float heightScale) {
        return new IrisFramebufferBuilderImpl(
                new FramebufferSize.Relative(widthScale, heightScale)
        );
    }

    @Override
    public IrisRenderer.Builder newRenderer(String name) {
        return new IrisRendererBuilderImpl(name, commonRenderers);
    }
}
