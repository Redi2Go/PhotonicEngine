package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.game.minecraft.Id;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public interface PipelineActionBuilder {
    default boolean addDebugGroup(String name) {
        return false;
    }

    default boolean addDeferredPass(
            String name,
            @Nullable IrisFramebuffer framebuffer,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            BiConsumer<IrisRenderer.CompositePassBuilder, Id> builderAction
    ) {
        return false;
    }

    default boolean addThenFlip(IrisFramebuffer... framebuffers) {
        return false;
    }

    default boolean addThenRun(Runnable action) {
        return false;
    }

    PipelineAction buildAction();
}
