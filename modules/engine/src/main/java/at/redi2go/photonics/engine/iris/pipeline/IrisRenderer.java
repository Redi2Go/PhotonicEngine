package at.redi2go.photonics.engine.iris.pipeline;

import at.redi2go.photonics.engine.iris.pipeline.buffers.IrisBufferHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.textures.ISamplerHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolderBuilder;
import at.redi2go.photonics.game.minecraft.Id;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IrisRenderer {
    void renderAll();

    interface Builder extends
            IrisDefineHolderBuilder<Builder>,
            IrisBufferHolderBuilder<Builder>,
            ISamplerHolderBuilder<Builder>,
            IrisDynamicUniformHolderBuilder<Builder>,
            IrisUniformHolderBuilder<Builder> {
        Builder withFragmentPrefix(@NonNull String prefix);

        Builder withVertexPrefix(@NonNull String prefix);

        Builder debugGroup(String name);

        default Builder debugGroup(String name, BooleanSupplier condition) {
            return condition.getAsBoolean() ? debugGroup(name) : this;
        }

        Builder withFramebuffer(IrisFramebuffer framebuffer);

        Builder deferredPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                BiConsumer<CompositePassBuilder, Id> builderAction
        );

        default Builder deferredPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                BooleanSupplier condition,
                BiConsumer<CompositePassBuilder, Id> builderAction
        ) {
            return condition.getAsBoolean() ? deferredPass(name, fragmentShader, vertexShader, builderAction) : this;
        }

        default Builder deferredPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader
        ) {
            return deferredPass(name, fragmentShader, vertexShader, (p0, dim) -> {});
        }

        default Builder deferredPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                BooleanSupplier condition
        ) {
            return condition.getAsBoolean() ? deferredPass(name, fragmentShader, vertexShader) : this;
        }

        Builder thenFlip(IrisFramebuffer... framebuffers);

        default Builder thenFlip(IrisFramebuffer framebuffer, BooleanSupplier condition) {
            return condition.getAsBoolean() ? thenFlip(framebuffer) : this;
        }

        Builder thenRun(Runnable action);

        default Builder thenRun(Runnable action, BooleanSupplier condition) {
            return condition.getAsBoolean() ? thenRun(action) : this;
        }

        Builder repeat(int n, IntObjectBiConsumer<IrisRenderer.Builder> builderAction);

        default Builder repeat(int n, BooleanSupplier condition, IntObjectBiConsumer<IrisRenderer.Builder> builderAction) {
            return condition.getAsBoolean() ? repeat(n, builderAction) : this;
        }

        Builder when(BooleanSupplier condition, Consumer<IrisRenderer.Builder> builderAction);

        IrisRenderer build(Function<IrisRenderer, IrisRenderer> registration);

        default IrisRenderer build() {
            return build(Function.identity());
        }
    }

    interface CompositePassBuilder extends IrisDefineHolder {

    }
}
