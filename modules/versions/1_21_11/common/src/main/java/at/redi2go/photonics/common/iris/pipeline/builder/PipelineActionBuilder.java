package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

public interface PipelineActionBuilder {
    default boolean addDebugGroup(String name) {
        return false;
    }

    default boolean addDeferredPass(String name, @Nullable IrisFramebuffer framebuffer, @Nullable String fragmentShader, @Nullable String vertexShader) {
        return false;
    }

    default boolean addThenFlip(IrisFramebuffer... framebuffers) {
        return false;
    }

    default boolean addThenRun(Runnable action) {
        return false;
    }

    default boolean addBeginRepeating(int n) {
        return false;
    }

    default boolean addEndRepeating() {
        return false;
    }

    PipelineAction buildAction();
}
