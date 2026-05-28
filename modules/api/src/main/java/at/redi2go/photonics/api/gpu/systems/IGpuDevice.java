package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public interface IGpuDevice {
    ICommandEncoder ph$createCommandEncoder();

    IGpuSampler ph$createSampler(
        IAddressMode addressModeU,
        IAddressMode addressModeV,
        IFilterMode minFilter,
        IFilterMode magFilter,
        int maxAnisotropy,
        OptionalDouble maxLod
    );

    IGpuTexture2D ph$createTexture2D(
            @Nullable Supplier<String> label,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            int width, int height,
            int mipLevels
    );

    IGpuTexture3D ph$createTexture3D(
            @Nullable Supplier<String> label,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            int width, int height, int depth,
            int mipLevels
    );

    IGpuBuffer ph$createBuffer(
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    );

    IGpuBufferHeap ph$createBufferHeap(
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    );
}
