package at.redi2go.photonics.common.iris.pipeline.builder.actions;

import at.redi2go.photonics.common.iris.pipeline.builder.PipelineActionBuilder;
import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;

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
