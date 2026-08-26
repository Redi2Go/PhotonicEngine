package at.redi2go.photonics.common.iris.pipeline.renderer;

public class EmptyIrisPassAction implements IrisPassAction {
    public static final EmptyIrisPassAction INSTANCE = new EmptyIrisPassAction();

    private EmptyIrisPassAction() {

    }

    @Override
    public void renderAll() {

    }
}
