package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class EmptyFramebuffer implements IrisFramebuffer, InternalIrisFramebuffer {
    private final static Vector2ic VIEWPORT_SIZE = new Vector2i(1, 1);

    public static final EmptyFramebuffer INSTANCE = new EmptyFramebuffer();

    private EmptyFramebuffer() {

    }

    @Override
    public Vector2ic viewportSize() {
        return VIEWPORT_SIZE;
    }

    @Override
    public void flip() {

    }

    @Override
    public void recalculateSizes() {

    }

    @Override
    public void bind() {

    }

    @Override
    public void unbind() {

    }
}
