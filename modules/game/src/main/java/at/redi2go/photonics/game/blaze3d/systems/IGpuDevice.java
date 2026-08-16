package at.redi2go.photonics.game.blaze3d.systems;

import at.redi2go.photonics.game.blaze3d.buffers.BufferUsage;
import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;
import at.redi2go.photonics.game.blaze3d.images.IImageFormat;
import at.redi2go.photonics.game.blaze3d.textures.IAddressMode;
import at.redi2go.photonics.game.blaze3d.textures.IFilterMode;
import at.redi2go.photonics.game.blaze3d.textures.IGpuSampler;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.ITextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.function.Supplier;

public interface IGpuDevice {
    static ICommandEncoder createCommandEncoder() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

   static IGpuSampler createSampler(
            IAddressMode addressModeU,
            IAddressMode addressModeV,
            IFilterMode minFilter,
            IFilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    ) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IGpuTexture createTexture(
            @Nullable Supplier<String> supplier,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            Vector3ic size,
            int mipLevels
    ) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IGpuTexture createTexture(
            @Nullable Supplier<String> supplier,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            Vector2ic size,
            int mipLevels
    ) {
        return createTexture(
                supplier,
                usage,
                textureFormat,
                new Vector3i(size, 1),
                mipLevels
        );
    }

    static IGpuTexture createImage(
            @Nullable Supplier<String> supplier,
            @TextureUsage int usage,
            IImageFormat imageFormat,
            Vector3ic size,
            int mipLevels
    ) {
        return createTexture(supplier, usage, imageFormat.ph$toTextureFormat(), size, mipLevels);
    }

    static IGpuTexture createImage(
            @Nullable Supplier<String> supplier,
            @TextureUsage int usage,
            IImageFormat imageFormat,
            Vector2ic size,
            int mipLevels
    ) {
        return createTexture(supplier, usage, imageFormat.ph$toTextureFormat(), size, mipLevels);
    }


    static IGpuBuffer createBuffer(
            @Nullable Supplier<String> supplier,
            @BufferUsage int usage,
            long byteSize
    ) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static IGpuBuffer createBuffer(
            @Nullable Supplier<String> supplier,
            @BufferUsage int usage,
            ByteBuffer contents
    ) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
