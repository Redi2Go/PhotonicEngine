package at.redi2go.photonics.common.iris.pipeline.builder.actions;

import at.redi2go.photonics.common.iris.pipeline.IrisFactoryImpl;
import at.redi2go.photonics.common.iris.pipeline.builder.AbstractActionBuilderConsumer;
import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RepeatAction(
        List<PipelineAction> actions,
        int count
) implements PipelineAction {
    @Override
    public void execute() {
        for (int i = 0; i < count; i++)
            actions.forEach(PipelineAction::execute);
    }

    public static class Builder extends AbstractActionBuilderConsumer {
        private final int count;
        private boolean hasEnded = false;

        public Builder(IrisFactoryImpl factory, int count, String debugGroup) {
            super(factory, debugGroup);

            this.count = count;
        }

        @Override
        public boolean addDebugGroup(String name) {
            if (hasEnded) return false;

            return super.addDebugGroup(name);
        }

        @Override
        public boolean addDeferredPass(String name, @Nullable IrisFramebuffer framebuffer, @Nullable String fragmentShader, @Nullable String vertexShader) {
            if (hasEnded) return false;

            return super.addDeferredPass(name, framebuffer, fragmentShader, vertexShader);
        }

        @Override
        public boolean addThenFlip(IrisFramebuffer... framebuffers) {
            if (hasEnded) return false;

            return super.addThenFlip(framebuffers);
        }

        @Override
        public boolean addThenRun(Runnable action) {
            if (hasEnded) return false;

            return super.addThenRun(action);
        }

        @Override
        public boolean addBeginRepeating(int n) {
            if (hasEnded) return false;

            return super.addBeginRepeating(n);
        }

        @Override
        public boolean addEndRepeating() {
            if (hasEnded) return false;

            var result = super.addEndRepeating();
            if (result) return true;

            hasEnded = true;
            return true;
        }

        @Override
        public PipelineAction buildAction() {
            if (!hasEnded) throw new IllegalStateException("repeat not complete");

            return new RepeatAction(buildActions(), count);
        }
    }
}
