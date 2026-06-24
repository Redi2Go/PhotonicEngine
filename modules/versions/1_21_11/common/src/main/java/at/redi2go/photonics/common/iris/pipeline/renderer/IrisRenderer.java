package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.common.iris.pipeline.impl.PipelineAction;

public interface IrisRenderer extends PipelineAction {
    void renderAll();

    @Override
    default void execute() {
        renderAll();
    }
}
