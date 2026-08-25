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

    IGpuTexture ph$createTexture(@Nullable Supplier<String> supplier, @TextureUsage int usage, TextureFormat textureFormat, Vector3ic size, int mipLevels);

    IGpuBuffer ph$createBuffer(@Nullable Supplier<String> supplier, @BufferUsage int usage, long byteSize);

    IGpuBuffer ph$createBuffer(@Nullable Supplier<String> supplier, @BufferUsage int usage, ByteBuffer contents);

    static ICommandEncoder createCommandEncoder() {
        return IRenderSystem.getDevice().ph$createCommandEncoder();
    }

    static IGpuSampler createSampler(IAddressMode addressModeU, IAddressMode addressModeV, IFilterMode minFilter, IFilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod) {
        return IRenderSystem.getDevice().ph$createSampler(addressModeU, addressModeV, minFilter, magFilter, maxAnisotropy, maxLod);
    }

    static IGpuTexture createTexture(@Nullable Supplier<String> supplier, @TextureUsage int usage, TextureFormat textureFormat, Vector3ic size, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(supplier, usage, textureFormat, size, mipLevels);
    }

    static IGpuTexture createTexture(@Nullable Supplier<String> supplier, @TextureUsage int usage, TextureFormat textureFormat, Vector2ic size, int mipLevels) {
        return IRenderSystem.getDevice().ph$createTexture(supplier, usage, textureFormat, new Vector3i(size, 1), mipLevels);
    }

    static IGpuBuffer createBuffer(@Nullable Supplier<String> supplier, @BufferUsage int usage, long byteSize) {
        return IRenderSystem.getDevice().ph$createBuffer(supplier, usage, byteSize);
    }

    static IGpuBuffer createBuffer(@Nullable Supplier<String> supplier, @BufferUsage int usage, ByteBuffer contents) {
        return IRenderSystem.getDevice().ph$createBuffer(supplier, usage, contents);
    }
}
