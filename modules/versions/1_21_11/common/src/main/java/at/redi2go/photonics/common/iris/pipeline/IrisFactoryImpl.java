package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.builder.IrisPipelineBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.FramebufferSize;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.IrisFramebufferBuilderImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererBuilder;
import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisRenderer;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipeline;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class IrisFactoryImpl implements IrisFactory {
    private final List<DeferredIrisRenderer> commonRenderers;

    public IrisFactoryImpl(List<DeferredIrisRenderer> commonRenderers) {
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

    public IrisRendererBuilder newRenderer(@NonNls String name) {
        Objects.requireNonNull(name, "name");
        return new IrisRendererBuilder(name, commonRenderers);
    }

    @Override
    public IrisPipeline.Builder newPipeline() {
        return new IrisPipelineBuilderImpl(this);
    }
}
