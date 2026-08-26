package at.redi2go.photonics.common.iris.pipeline.renderer;


import at.redi2go.photonics.common.iris.pipeline.builder.PipelineActionBuilder;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.engine.iris.rendering.Pipelines;
import at.redi2go.photonics.game.minecraft.Id;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class IrisRendererBuilder implements PipelineActionBuilder {
    private final String name;

    private final List<DeferredIrisPassAction.Pass> passes = new ArrayList<>();
    private final List<DeferredIrisPassAction> commonRenderers;

    private boolean finished = false;

    public IrisRendererBuilder(
            String name,
            List<DeferredIrisPassAction> commonRenderers
    ) {
        this.name = name;
        this.commonRenderers = commonRenderers;
    }

    @Override
    public boolean addDebugGroup(String name) {
        finished = true;
        return false;
    }

    @Override
    public boolean addDeferredPass(
            String name,
            @Nullable IrisFramebuffer framebuffer,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            BiConsumer<IrisRenderer.CompositePassBuilder, Id> builderAction
    ) {
        if (finished) return false;

        passes.add(
                new DeferredIrisPassAction.Pass(
                        name,
                        fragmentShader,
                        vertexShader == null ? Pipelines.DEFAULT_VERTEX_SHADER : vertexShader,
                        framebuffer,
                        builderAction,
                        new ArrayList<>()
                )
        );

        return true;
    }

    @Override
    public boolean addThenFlip(IrisFramebuffer... framebuffers) {
        if (finished) return false;
        if (passes.isEmpty()) return false;

        passes.getLast().actions().add(() -> {
            for (var framebuffer : framebuffers)
                framebuffer.flip();
        });

        return true;
    }

    @Override
    public boolean addThenRun(Runnable action) {
        if (finished) return false;
        if (passes.isEmpty()) return false;

        passes.getLast().actions().add(action);

        return true;
    }

    public IrisPassAction buildAction() {
        if (passes.isEmpty()) return EmptyIrisPassAction.INSTANCE;

        var result = new DeferredIrisPassAction(name, passes);
        commonRenderers.add(result);

        return result;
    }
}
