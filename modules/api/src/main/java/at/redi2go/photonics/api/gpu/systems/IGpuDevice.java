package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.function.Supplier;

public interface IGpuDevice {
    ICommandEncoder createCommandEncoder();

    IGpuSampler createSampler(
        IAddressMode addressModeU,
        IAddressMode addressModeV,
        IFilterMode minFilter,
        IFilterMode magFilter,
        int maxAnisotropy,
        OptionalDouble maxLod
    );

    IGpuTexture createTexture(
            @Nullable Supplier<String> label,
            @TextureUsage int usage,
            ITextureFormat textureFormat,
            int width,
            int height,
            int depthOrLayers,
            int mipLevels
    );

    IGpuBuffer createBuffer(
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    );

    IGpuBufferHeap createBufferHeap(
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    );
}
