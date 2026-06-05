package at.redi2go.photonics.core.iris.pipeline.rendering;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public interface IrisRenderer {
    void renderAll();

    interface Builder {
        Builder addPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                @Nullable IrisFramebuffer framebuffer
        );

        default Builder addPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                @Nullable IrisFramebuffer framebuffer,
                BooleanSupplier condition
        ) {
            return condition.getAsBoolean() ? addPass(name, fragmentShader, vertexShader, framebuffer) : this;
        }

        IrisRenderer build();
    }
}
