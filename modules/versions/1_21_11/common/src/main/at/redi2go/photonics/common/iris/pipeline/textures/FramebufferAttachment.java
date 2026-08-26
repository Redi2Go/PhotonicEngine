package at.redi2go.photonics.common.iris.pipeline.textures;

import at.redi2go.photonics.game.Disposable;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import org.joml.Vector2ic;

public record FramebufferAttachment(
        String name,
        IGpuTexture texture,
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
