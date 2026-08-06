package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface CompositeRendererPassExt {
    String getDebugName();

    void setDebugName(@Nullable String name);

    int getIndex();

    void setIndex(int index);

    /** Run after the pass is completed */
    ImmutableList<Runnable> getActions();

    void setActions(List<Runnable> actions);

    Optional<IrisFramebuffer> getFramebuffer();

    void setFramebuffer(@Nullable IrisFramebuffer framebuffer);

    void updateSize();









}
