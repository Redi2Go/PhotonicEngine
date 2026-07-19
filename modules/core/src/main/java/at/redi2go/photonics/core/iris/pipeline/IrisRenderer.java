package at.redi2go.photonics.core.iris.pipeline;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import it.unimi.dsi.fastutil.ints.IntObjectBiConsumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IrisRenderer {
    void renderAll();

    interface Builder {
        Builder withFragmentPrefix(@NonNls String prefix);

        Builder withVertexPrefix(@NonNls String prefix);

        Builder debugGroup(String name);

        default Builder debugGroup(String name, BooleanSupplier condition) {
            return condition.getAsBoolean() ? debugGroup(name) : this;
        }

        Builder withFramebuffer(IrisFramebuffer framebuffer);

        Builder deferredPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader
        );

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
}
