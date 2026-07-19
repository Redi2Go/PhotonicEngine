package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;
import at.redi2go.photonics.core.iris.pipeline.IrisRenderer;

import java.util.List;

public record IrisRendererImpl(List<PipelineAction> actions) implements IrisRenderer {
    @Override
    public void renderAll() {
        actions.forEach(PipelineAction::execute);
    }
}
