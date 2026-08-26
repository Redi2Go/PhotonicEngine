package at.redi2go.photonics.common.iris.pipeline.builder.actions;

import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.common.iris.pipeline.builder.PipelineActionBuilder;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;

public record FlipAction(IrisFramebuffer[] framebuffers) implements PipelineAction, PipelineActionBuilder {
    @Override
    public void execute() {
        for (var framebuffer : framebuffers)
            framebuffer.flip();
    }

    @Override
    public PipelineAction buildAction() {
        return this;
    }
}
