package at.redi2go.photonics.common.iris.pipeline.renderer;

import at.redi2go.photonics.core.iris.pipeline.rendering.IrisRenderer;

public class EmptyRenderer implements IrisRenderer {
    public static final EmptyRenderer INSTANCE = new EmptyRenderer();

    private EmptyRenderer() {

    }

    @Override
    public void renderAll() {

    }
}
