package at.redi2go.photonics.engine.iris.pipeline;

import at.redi2go.photonics.engine.iris.pipeline.buffer.IBufferHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IDefineHolder;
import at.redi2go.photonics.engine.iris.pipeline.defines.IDefineHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.texture.ISamplerHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.texture.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.pipeline.uniform.IDynamicUniformHolderBuilder;
import at.redi2go.photonics.engine.iris.pipeline.uniform.IUniformHolderBuilder;
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
            IDefineHolderBuilder<Builder>,
            IBufferHolderBuilder<Builder>,
            ISamplerHolderBuilder<Builder>,
            IDynamicUniformHolderBuilder<Builder>,
            IUniformHolderBuilder<Builder> {
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
                BiConsumer<PassBuilder, Id> builderAction
        );

        default Builder deferredPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                BooleanSupplier condition,
                BiConsumer<PassBuilder, Id> builderAction
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

    interface PassBuilder extends IDefineHolder {

    }
}
