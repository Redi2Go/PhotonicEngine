package at.redi2go.photonics.common.iris.pipeline.textures;

import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import org.joml.Vector2ic;

public interface InternalIrisFramebuffer extends IrisFramebuffer {
    Vector2ic viewportSize();

    void bind();

    void unbind();
}
