package at.redi2go.photonics.common.iris.pipeline.renderer;

public class EmptyIrisRenderer implements IrisRenderer {
    public static final EmptyIrisRenderer INSTANCE = new EmptyIrisRenderer();

    private EmptyIrisRenderer() {

    }

    @Override
    public void renderAll() {

    }
}
