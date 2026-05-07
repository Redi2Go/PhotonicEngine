package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

import java.util.Objects;

public record FramebufferAttachment(
        String name,
        IGpuTexture2D texture,
        boolean createSampler,
        boolean createPrevSampler
) implements Disposable {
    public void resize(Vector2ic newSize) {
        texture.resize(newSize);
    }

    @Override
    public void close() {
        texture.close();
    }
}
