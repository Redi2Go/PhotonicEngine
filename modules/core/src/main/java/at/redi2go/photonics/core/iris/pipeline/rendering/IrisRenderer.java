package at.redi2go.photonics.core.iris.pipeline.rendering;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

public interface IrisRenderer {
    void renderAll();

    interface Builder {
        Builder addPass(
                String name,
                @Nullable String fragmentShader,
                @Nullable String vertexShader,
                @Nullable IrisFramebuffer framebuffer
        );

        IrisRenderer build();
    }
}
