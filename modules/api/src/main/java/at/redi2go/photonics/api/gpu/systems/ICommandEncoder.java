package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;

import java.nio.ByteBuffer;

public interface ICommandEncoder {
    void clearColorTexture(IGpuTexture gpuTexture, int clearColor);

    void writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer);

    void writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer);

    IGpuBuffer.MappedView mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable);

    IGpuBuffer.MappedView mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable);

    void copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2);

    void writeToTexture(
            IGpuTexture gpuTexture,
            ByteBuffer data,
            ITextureFormat format,
            int mipLevels,
            int cubeMapTarget,
            int offsetX,
            int offsetY,
            int width,
            int height
    );
}
