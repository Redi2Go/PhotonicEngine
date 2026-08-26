package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.common.iris.pipeline.PipelineAction;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;

import java.util.List;

public record IrisRendererImpl(List<PipelineAction> actions) implements IrisRenderer {
    @Override
    public void renderAll() {
        actions.forEach(PipelineAction::execute);
    }
}
