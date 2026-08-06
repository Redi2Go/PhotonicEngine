package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.IrisFactoryImpl;
import at.redi2go.photonics.common.iris.pipeline.IrisPipelineImpl;
import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipeline;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class IrisPipelineBuilderImpl extends AbstractActionBuilderConsumer implements IrisPipeline.Builder {
    private @Nullable IrisFramebuffer framebuffer = null;

    private @NonNls String fragmentPrefix = "";
    private @NonNls String vertexPrefix = "";

    public IrisPipelineBuilderImpl(IrisFactoryImpl factory) {
        super(factory, "Photonics");
    }

    private void scope(Consumer<IrisPipeline.Builder> builderAction) {
        var oldFrameBuffer = framebuffer;
        var oldFragmentPrefix = fragmentPrefix;
        var oldVertexPrefix = vertexPrefix;

        builderAction.accept(this);

        framebuffer = oldFrameBuffer;
        fragmentPrefix = oldFragmentPrefix;
        vertexPrefix = oldVertexPrefix;
    }

    @Override
    public IrisPipeline.Builder withFragmentPrefix(@NonNls String prefix) {
        Objects.requireNonNull(prefix, "fragment prefix was null");
        fragmentPrefix = prefix;

        return this;
    }

    @Override
    public IrisPipeline.Builder withVertexPrefix(@NonNls String prefix) {
        Objects.requireNonNull(prefix, "vertex prefix was null");
        vertexPrefix = prefix;

        return this;
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
        addDeferredPass(
                name,
                framebuffer,
                fragmentShader != null ? fragmentPrefix + fragmentShader : null,
                vertexShader != null ? vertexPrefix + vertexShader : null
        );

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
    public IrisPipeline.Builder repeat(int n, IntObjectBiConsumer<IrisPipeline.Builder> builderAction) {
        for (int i = 0; i < n; i++) {
            final int index = i;
            scope((b0) -> builderAction.accept(index, b0));
        }

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
