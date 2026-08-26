package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.IrisPipelineImpl;
import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisPassAction;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererImpl;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolder;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisSamplerHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;
import at.redi2go.photonics.game.minecraft.Id;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class IrisPipelineBuilderImpl extends AbstractActionBuilderConsumer implements IrisRenderer.Builder, BiConsumer<IrisDefineHolder, Id> {
    private final List<BiConsumer<IrisDefineHolder, Id>> defines;
    private @Nullable IrisFramebuffer framebuffer = null;

    private final Consumer<IrisRenderer> registration;

    private @NonNls String fragmentPrefix = "";
    private @NonNls String vertexPrefix = "";

    public IrisPipelineBuilderImpl(IrisPipelineImpl factory, Consumer<IrisRenderer> registration) {
        super(factory, "Photonics");

        this.defines = new ArrayList<>();
        this.registration = registration;
    }

    @Override
    public IrisRenderer.Builder withDefine(BiConsumer<IrisDefineHolder, Id> consumer) {
        defines.add(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withDefines(List<BiConsumer<IrisDefineHolder, Id>> consumers) {
        defines.addAll(consumers);

        return this;
    }

    @Override
    public void accept(IrisDefineHolder irisDefineHolder, Id id) {
        for (var define : defines)
            define.accept(irisDefineHolder, id);
    }

    @Override
    public IrisRenderer.Builder withBuffer(Consumer<IrisBufferHolder> consumer) {
        factory.withBuffer(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withSampler(Consumer<IrisSamplerHolder> consumer) {
        factory.withSampler(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withDynamicUniform(Consumer<IrisDynamicUniformHolder> consumer) {
        factory.withDynamicUniform(consumer);

        return this;
    }

    @Override
    public IrisRenderer.Builder withUniform(Consumer<IrisUniformHolder> consumer) {
        factory.withUniform(consumer);

        return this;
    }

    private void scope(Consumer<IrisRenderer.Builder> builderAction) {
        var oldFrameBuffer = framebuffer;
        var oldFragmentPrefix = fragmentPrefix;
        var oldVertexPrefix = vertexPrefix;

        builderAction.accept(this);

        framebuffer = oldFrameBuffer;
        fragmentPrefix = oldFragmentPrefix;
        vertexPrefix = oldVertexPrefix;
    }

    @Override
    public IrisRenderer.Builder withFragmentPrefix(@NonNls String prefix) {
        Objects.requireNonNull(prefix, "fragment prefix was null");
        fragmentPrefix = prefix;

        return this;
    }

    @Override
    public IrisRenderer.Builder withVertexPrefix(@NonNls String prefix) {
        Objects.requireNonNull(prefix, "vertex prefix was null");
        vertexPrefix = prefix;

        return this;
    }

    @Override
    public IrisRenderer.Builder debugGroup(String name) {
        addDebugGroup(name);

        return this;
    }

    @Override
    public IrisRenderer.Builder withFramebuffer(IrisFramebuffer framebuffer) {
        this.framebuffer = framebuffer;
        return this;
    }

    @Override
    public IrisRenderer.Builder deferredPass(
            String name,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            BiConsumer<IrisRenderer.CompositePassBuilder, Id> builderAction
    ) {
        addDeferredPass(
                name,
                framebuffer,
                fragmentShader != null ? fragmentPrefix + fragmentShader : null,
                vertexShader != null ? vertexPrefix + vertexShader : null,
                builderAction.andThen(this)
        );

        return this;
    }

    @Override
    public IrisRenderer.Builder thenFlip(IrisFramebuffer... framebuffers) {
        addThenFlip(framebuffers);

        return this;
    }

    @Override
    public IrisRenderer.Builder thenRun(Runnable action) {
        addThenRun(action);

        return this;
    }

    @Override
    public IrisRenderer.Builder repeat(int n, IntObjectBiConsumer<IrisRenderer.Builder> builderAction) {
        for (int i = 0; i < n; i++) {
            final int index = i;
            scope((b0) -> builderAction.accept(index, b0));
        }

        return this;
    }

    @Override
    public IrisRenderer.Builder when(BooleanSupplier condition, Consumer<IrisRenderer.Builder> builderAction) {
        if (condition.getAsBoolean())
            scope(builderAction);

        return this;
    }

    @Override
    public IrisRenderer build() {
        var result = new IrisRendererImpl(buildActions());
        registration.accept(result);

        return result;
    }

    @Override
    public PipelineAction buildAction() {
        throw new UnsupportedOperationException("buildActions");
    }
}
