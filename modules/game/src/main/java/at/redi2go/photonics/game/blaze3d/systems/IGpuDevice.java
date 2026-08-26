package at.redi2go.photonics.game.blaze3d.systems;

import at.redi2go.photonics.game.blaze3d.buffers.BufferUsage;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.textures.IAddressMode;
import at.redi2go.photonics.game.blaze3d.textures.IFilterMode;
import at.redi2go.photonics.game.blaze3d.textures.IGpuSampler;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import org.jspecify.annotations.Nullable;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.function.Supplier;

public interface IGpuDevice {
    ICommandEncoder ph$createCommandEncoder();

    IGpuSampler ph$createSampler(IAddressMode addressModeU, IAddressMode addressModeV, IFilterMode minFilter, IFilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod);

    IGpuTexture ph$createTexture(@Nullable Supplier<String> label, @TextureUsage int usage, TextureFormat textureFormat, Vector3ic size, int mipLevels);

    IGpuBuffer ph$createBuffer(@Nullable Supplier<String> label, @BufferUsage int usage, long byteSize);

    IGpuBuffer ph$createBuffer(@Nullable Supplier<String> label, @BufferUsage int usage, ByteBuffer contents);

    static ICommandEncoder createCommandEncoder() {
        return IRenderSystem.getDevice().ph$createCommandEncoder();
    }

    static IGpuSampler createSampler(IAddressMode addressModeU, IAddressMode addressModeV, IFilterMode minFilter, IFilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        return IRenderSystem.getDevice().ph$createSampler(addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
    }

    static IGpuTexture createTexture(@Nullable Supplier<String> label, @TextureUsage int usage, TextureFormat textureFormat, Vector3ic size, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(label, usage, textureFormat, size, mipLevels);
    }

    static IGpuTexture createTexture(@Nullable Supplier<String> label, @TextureUsage int usage, TextureFormat textureFormat, Vector2ic size, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(label, usage, textureFormat, new Vector3i(size, 0), mipLevels);
    }

    static IGpuTexture createTexture(@Nullable Supplier<String> label, @TextureUsage int usage, TextureFormat textureFormat, int width, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(label, usage, textureFormat, new Vector3i(width, 0, 0), mipLevels);
    }

    static IGpuBuffer createBuffer(@Nullable Supplier<String> label, @BufferUsage int usage, long byteSize) {
        return IRenderSystem.getDevice().ph$createBuffer(label, usage, byteSize);
    }

    static IGpuTexture createTexture(@Nullable String label, @TextureUsage int usage, TextureFormat textureFormat, Vector3ic size, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(() -> label, usage, textureFormat, size, mipLevels);
    }

    static IGpuTexture createTexture(@Nullable String label, @TextureUsage int usage, TextureFormat textureFormat, Vector2ic size, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(() -> label, usage, textureFormat, new Vector3i(size, 0), mipLevels);
    }

    static IGpuTexture createTexture(@Nullable String label, @TextureUsage int usage, TextureFormat textureFormat, int width, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(() -> label, usage, textureFormat, new Vector3i(width, 0, 0), mipLevels);
    }

    static IGpuBuffer createBuffer(@Nullable String label, @BufferUsage int usage, long byteSize) {
        return IRenderSystem.getDevice().ph$createBuffer(() -> label, usage, byteSize);
    }

    static IGpuBuffer createBuffer(@Nullable String label, @BufferUsage int usage, ByteBuffer contents) {
        return IRenderSystem.getDevice().ph$createBuffer(() -> label, usage, contents);
    }
}
