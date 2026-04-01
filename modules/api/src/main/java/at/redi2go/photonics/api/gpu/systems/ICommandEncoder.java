package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.IGpuBufferSlice;

import java.nio.ByteBuffer;

public interface ICommandEncoder {
    void writeToBuffer(IGpuBuffer buffer, ByteBuffer byteBuffer);

    void writeToBuffer(IGpuBufferSlice slice, ByteBuffer byteBuffer);

    IGpuBuffer.MappedView mapBuffer(IGpuBuffer buffer, boolean readable, boolean writeable);

    IGpuBuffer.MappedView mapBuffer(IGpuBufferSlice bufferSlice, boolean readable, boolean writeable);

    void copyToBuffer(IGpuBufferSlice slice1, IGpuBufferSlice slice2);
}
