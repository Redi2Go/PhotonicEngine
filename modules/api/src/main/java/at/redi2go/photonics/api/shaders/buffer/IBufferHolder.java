package at.redi2go.photonics.api.shaders.buffer;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;

import java.util.function.Supplier;

public interface IBufferHolder {
    void addDefaultBuffer(String name, Supplier<IGpuBuffer> buffer);
}
