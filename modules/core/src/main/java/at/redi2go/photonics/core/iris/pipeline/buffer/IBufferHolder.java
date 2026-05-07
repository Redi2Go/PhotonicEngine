package at.redi2go.photonics.core.iris.pipeline.buffer;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;

import java.util.function.Supplier;

public interface IBufferHolder {
    void addDefaultBuffer(String name, Supplier<IGpuBuffer> buffer);

    void addDefaultBufferHeap(String name, Supplier<IGpuBufferHeap> buffer);
}
