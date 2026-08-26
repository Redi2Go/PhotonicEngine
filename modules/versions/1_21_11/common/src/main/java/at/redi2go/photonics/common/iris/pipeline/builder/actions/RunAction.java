package at.redi2go.photonics.common.iris.pipeline.builder.actions;

import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.common.iris.pipeline.builder.PipelineActionBuilder;

public record RunAction(Runnable action) implements PipelineAction, PipelineActionBuilder {
    @Override
    public void execute() {
        action.run();
    }

    @Override
    public PipelineAction buildAction() {
        return this;
    }
}
