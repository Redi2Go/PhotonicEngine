package at.redi2go.photonics.core.iris.pipeline.rendering;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IrisPipeline {
    void renderAll();

    interface Builder {
        Builder debugGroup(String name);

        default Builder debugGroup(String name, BooleanSupplier condition) {
            return condition.getAsBoolean() ? debugGroup(name) : this;
        }

        Builder deferredPass(
                String name,
                @Nullable IrisFramebuffer framebuffer,
                @Nullable String fragmentShader,
                @Nullable String vertexShader
        );

        default Builder deferredPass(
                String name,
                @Nullable IrisFramebuffer framebuffer,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                BooleanSupplier condition
        ) {
            return condition.getAsBoolean() ? deferredPass(name, framebuffer, fragmentShader, vertexShader) : this;
        }

        Builder thenFlip(IrisFramebuffer... framebuffers);

        default Builder thenFlip(BooleanSupplier condition, IrisFramebuffer... framebuffers) {
            return condition.getAsBoolean() ? thenFlip(framebuffers) : this;
        }

        Builder thenRun(Runnable action);

        default Builder thenRun(Runnable action, BooleanSupplier condition) {
            return condition.getAsBoolean() ? thenRun(action) : this;
        }

        Builder repeat(int n, Consumer<IrisPipeline.Builder> builderAction);

        default Builder repeat(int n, BooleanSupplier condition, Consumer<IrisPipeline.Builder> builderAction) {
            return condition.getAsBoolean() ? repeat(n, builderAction) : this;
        }

        default Builder when(BooleanSupplier condition, Consumer<IrisPipeline.Builder> builderAction) {
            if (condition.getAsBoolean()) {
                builderAction.accept(this);
            }

            return this;
        }

        IrisPipeline build(Function<IrisPipeline, IrisPipeline> registration);

        default IrisPipeline build() {
            return build(Function.identity());
        }
    }
}
