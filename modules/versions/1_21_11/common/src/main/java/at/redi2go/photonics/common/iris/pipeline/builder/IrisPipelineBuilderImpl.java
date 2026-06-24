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
    public IrisPipelineBuilderImpl(IrisFactoryImpl factory) {
        super(factory, "Photonics");
    }

    @Override
    public IrisPipeline.Builder debugGroup(String name) {
        addDebugGroup(name);

        return this;
    }

    @Override
    public IrisPipeline.Builder deferredPass(String name, @Nullable IrisFramebuffer framebuffer, @Nullable String fragmentShader, @Nullable String vertexShader) {
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
        builderAction.accept(this);
        addEndRepeating();

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
