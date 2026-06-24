package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.IrisFactoryImpl;
import at.redi2go.photonics.common.iris.pipeline.IrisPipelineImpl;
import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisFactory;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipeline;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class IrisPipelineBuilderImpl extends AbstractActionBuilderConsumer implements IrisPipeline.Builder {
    private @Nullable IrisFramebuffer framebuffer = null;

    public IrisPipelineBuilderImpl(IrisFactoryImpl factory) {
        super(factory, "Photonics");
    }

    private void scope(Consumer<IrisPipeline.Builder> builderAction) {
        var oldFrameBuffer = framebuffer;
        builderAction.accept(this);
        framebuffer = oldFrameBuffer;
    }

    @Override
    public IrisPipeline.Builder debugGroup(String name) {
        addDebugGroup(name);

        return this;
    }

    @Override
    public IrisPipeline.Builder withFramebuffer(IrisFramebuffer framebuffer) {
        this.framebuffer = framebuffer;
        return this;
    }

    @Override
    public IrisPipeline.Builder deferredPass(String name, @Nullable String fragmentShader, @Nullable String vertexShader) {
        addDeferredPass(name, framebuffer, fragmentShader, vertexShader);

        return this;
    }

    @Override
    public IrisPipeline.Builder thenFlip(IrisFramebuffer... framebuffers) {
        addThenFlip(framebuffers);

        return this;
    }

    @Override
    public IrisPipeline.Builder thenRun(Runnable action) {
        addThenRun(action);

        return this;
    }

    @Override
    public IrisPipeline.Builder repeat(int n, Consumer<IrisPipeline.Builder> builderAction) {
        addBeginRepeating(n);
        scope(builderAction);
        addEndRepeating();

        return this;
    }

    @Override
    public IrisPipeline.Builder when(BooleanSupplier condition, Consumer<IrisPipeline.Builder> builderAction) {
        if (condition.getAsBoolean())
            scope(builderAction);

        return this;
    }

    @Override
    public IrisPipeline build(Function<IrisPipeline, IrisPipeline> registration) {
        return registration.apply(new IrisPipelineImpl(buildActions()));
    }

    @Override
    public PipelineAction buildAction() {
        throw new UnsupportedOperationException("buildActions");
    }
}
