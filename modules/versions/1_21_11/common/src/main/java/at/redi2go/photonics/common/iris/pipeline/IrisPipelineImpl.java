package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisPipeline;

import java.util.List;

public record IrisPipelineImpl(List<PipelineAction> actions) implements IrisPipeline {
    @Override
    public void renderAll() {
        actions.forEach(PipelineAction::execute);
    }
}
