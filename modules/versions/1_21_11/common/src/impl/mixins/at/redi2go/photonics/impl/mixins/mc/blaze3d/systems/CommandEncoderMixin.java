package at.redi2go.photonics.impl.mixins.mc.blaze3d.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.textures.GpuTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(CommandEncoder.class)
public interface CommandEncoderMixin extends ICommandEncoder, CommandEncoder {
    @Override
    default void clearColorTexture(IGpuTexture gpuTexture, int clearColor) {
        clearColorTexture((GpuTexture) gpuTexture, clearColor);
    }

    @Override
    default void writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer) {
        writeToBuffer(((GpuBuffer) buffer).slice(), byteBuffer);
    }

    @Override
    default void writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer) {
        writeToBuffer((GpuBufferSlice) (Object) slice, byteBuffer);
    }

    @Override
    default IGpuBuffer.MappedView mapBuffer(IGpuBuffer buffer, boolean readable, boolean writable) {
        return (IGpuBuffer.MappedView) mapBuffer((GpuBuffer) buffer, readable, writable);
    }

    @Override
    default IGpuBuffer.MappedView mapBuffer(IGpuBufferSlice buffer, boolean readable, boolean writable) {
        return (IGpuBuffer.MappedView) mapBuffer((GpuBufferSlice) (Object) buffer, readable, writable);
    }

    @Override
    default void copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2) {
        copyToBuffer((GpuBufferSlice) (Object) slice1, (GpuBufferSlice) (Object) slice2);
    }

    @Override
    default void writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer data,
            ITextureFormat format,
            int mipLevels,
            int cubeMapTarget,
            int offsetX,
            int offsetY,
            int width,
            int height
    ) {
        // Needs to be done in api specific encoder
        throw new UnsupportedOperationException("writeToTexture");
    }
}
