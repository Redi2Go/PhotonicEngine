package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;

public class EmptyFramebuffer implements IrisFramebuffer {
    public static final EmptyFramebuffer INSTANCE = new EmptyFramebuffer();

    private EmptyFramebuffer() {

    }

    @Override
    public void flip() {

    }

    @Override
    public void recalculateSizes() {

    }
}
