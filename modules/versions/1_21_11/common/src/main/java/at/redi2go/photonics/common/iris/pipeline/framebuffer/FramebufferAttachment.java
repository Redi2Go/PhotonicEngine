package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import org.joml.Vector2ic;

public record FramebufferAttachment(
        String name,
        IGpuTexture2D texture,
        boolean createSampler,
        boolean createPrevSampler
) implements Disposable {
    public void resize(Vector2ic newSize) {
        texture.ph$resize(newSize);
    }

    @Override
    public void close() {
        texture.close();
    }
}
