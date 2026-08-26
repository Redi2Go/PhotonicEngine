package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.IrisPipelineImpl;
import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.common.iris.pipeline.builder.actions.FlipAction;
import at.redi2go.photonics.common.iris.pipeline.builder.actions.RunAction;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import at.redi2go.photonics.game.minecraft.Id;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class AbstractActionBuilderConsumer implements PipelineActionBuilder {
    protected final IrisPipelineImpl factory;
    private final List<PipelineActionBuilder> actions = new ArrayList<>();

    private String currentDebugGroup;

    protected AbstractActionBuilderConsumer(IrisPipelineImpl factory, String debugGroup) {
        this.factory = factory;
        this.currentDebugGroup = debugGroup;
    }

    private boolean shouldCreateAction(Predicate<PipelineActionBuilder> predicate) {
        var action = lastAction();
        return action == null || !predicate.test(action);
    }

    private @Nullable PipelineActionBuilder lastAction() {
        return actions.isEmpty() ? null : actions.getLast();
    }

    @Override
    public boolean addDebugGroup(String name) {
        if (shouldCreateAction(e -> e.addDebugGroup(name))) {
            currentDebugGroup = name;
        }

        return true;
    }

    @Override
    public boolean addDeferredPass(
            String name,
            @Nullable IrisFramebuffer framebuffer,
            @Nullable String fragmentShader,
            @Nullable String vertexShader,
            BiConsumer<IrisRenderer.CompositePassBuilder, Id> builderAction
    ) {
        if (shouldCreateAction(e -> e.addDeferredPass(name, framebuffer, fragmentShader, vertexShader, builderAction))) {
            var pass = factory.newRenderer(currentDebugGroup);
            pass.addDeferredPass(name, framebuffer, fragmentShader, vertexShader, builderAction);

            actions.add(pass);
        }

        return true;
    }

    @Override
    public boolean addThenFlip(IrisFramebuffer... framebuffers) {
        if (shouldCreateAction(e -> e.addThenFlip(framebuffers))) {
            actions.add(new FlipAction(framebuffers));
        }

        return true;
    }

    @Override
    public boolean addThenRun(Runnable action) {
        if (shouldCreateAction(e -> e.addThenRun(action))) {
            actions.add(new RunAction(action));
        }

        return true;
    }

    protected final List<PipelineAction> buildActions() {
        var builder = ImmutableList.<PipelineAction>builder();

        for (var actionBuilder : actions)
            builder.add(actionBuilder.buildAction());

        return builder.build();
    }
}
