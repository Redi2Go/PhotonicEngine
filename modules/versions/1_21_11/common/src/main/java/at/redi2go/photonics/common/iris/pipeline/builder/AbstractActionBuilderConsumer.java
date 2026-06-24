package at.redi2go.photonics.common.iris.pipeline.builder;

import at.redi2go.photonics.common.iris.pipeline.IrisFactoryImpl;
import at.redi2go.photonics.common.iris.pipeline.builder.actions.FlipAction;
import at.redi2go.photonics.common.iris.pipeline.builder.actions.RepeatAction;
import at.redi2go.photonics.common.iris.pipeline.builder.actions.RunAction;
import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractActionBuilderConsumer implements PipelineActionBuilder {
    protected final IrisFactoryImpl factory;
    private final List<PipelineActionBuilder> actions = new ArrayList<>();

    private String currentDebugGroup;

    protected AbstractActionBuilderConsumer(IrisFactoryImpl factory, String debugGroup) {
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
    public boolean addDeferredPass(String name, @Nullable IrisFramebuffer framebuffer, @Nullable String fragmentShader, @Nullable String vertexShader) {
        if (shouldCreateAction(e -> e.addDeferredPass(name, framebuffer, fragmentShader, vertexShader))) {
            var pass = factory.newRenderer(currentDebugGroup);
            pass.addDeferredPass(name, framebuffer, fragmentShader, vertexShader);

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

    @Override
    public boolean addBeginRepeating(int n) {
        if (shouldCreateAction(e -> e.addBeginRepeating(n))) {
            actions.add(new RepeatAction.Builder(factory, n, currentDebugGroup));
        }

        return true;
    }

    @Override
    public boolean addEndRepeating() {
        return !shouldCreateAction(PipelineActionBuilder::addEndRepeating);
    }

    protected final List<PipelineAction> buildActions() {
        var builder = ImmutableList.<PipelineAction>builder();

        for (var actionBuilder : actions)
            builder.add(actionBuilder.buildAction());

        return builder.build();
    }
}
