package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.common.iris.pipeline.PipelineAction;

public interface IrisPassAction extends PipelineAction {
    void renderAll();

    @Override
    default void execute() {
        renderAll();
    }
}
