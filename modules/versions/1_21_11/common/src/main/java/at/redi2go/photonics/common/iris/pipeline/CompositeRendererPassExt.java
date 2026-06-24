package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface CompositeRendererPassExt {
    int index();

    void setIndex(int index);

    Optional<IrisFramebuffer> getFramebuffer();

    void setFramebuffer(@Nullable IrisFramebuffer framebuffer);

    void updateSize();
}
