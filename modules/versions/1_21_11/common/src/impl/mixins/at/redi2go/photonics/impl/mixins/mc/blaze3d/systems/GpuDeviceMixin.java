package at.redi2go.photonics.impl.mixins.mc.blaze3d.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.DefaultGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalDouble;
import java.util.function.Supplier;

@Mixin(GpuDevice.class)
public interface GpuDeviceMixin extends IGpuDevice {
    @Shadow CommandEncoder shadow$createCommandEncoder();

    @Override
    default ICommandEncoder createCommandEncoder() {
        return (ICommandEncoder) shadow$createCommandEncoder();
    }

    @Shadow GpuSampler shadow$createSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod);

    @Override
    default IGpuSampler createSampler(
            IAddressMode addressModeU,
            IAddressMode addressModeV,
            IFilterMode minFilter,
            IFilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    ) {
        return (IGpuSampler) shadow$createSampler(
                (AddressMode) (Object) addressModeU,
                (AddressMode) (Object) addressModeV,
                (FilterMode) (Object) minFilter,
                (FilterMode) (Object) magFilter,
                maxAnisotropy,
                maxLod
        );
    }

    @Shadow GpuTexture shadow$createTexture(@Nullable Supplier<String> label, int usage, TextureFormat textureFormat, int width, int height, int depthOrLayers, int mipLevels);

    @Override
    default IGpuTexture createTexture(
            @Nullable Supplier<String> label,
            int usage,
            ITextureFormat textureFormat,
            int width,
            int height,
            int depthOrLayers,
            int mipLevels
    ) {
        return (IGpuTexture) shadow$createTexture(
                label,
                usage,
                (TextureFormat) (Object) textureFormat,
                width,
                height,
                depthOrLayers,
                mipLevels
        );
    }

    @Shadow GpuBuffer shadow$createBuffer(@Nullable Supplier<String> label, int usage, long byteSize);

    @Override
    default IGpuBuffer createBuffer(@Nullable Supplier<String> label, long byteSize, int usage) {
        return (IGpuBuffer) shadow$createBuffer(
                label,
                usage,
                byteSize
        );
     }

    @Override
    default IGpuBufferHeap createBufferHeap(@Nullable Supplier<String> label, long byteSize, int usage) {
        return new DefaultGpuBufferHeap(this, label, byteSize, usage);
    }
}
