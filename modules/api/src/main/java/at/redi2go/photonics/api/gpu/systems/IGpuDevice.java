package at.redi2go.photonics.api.gpu.systems;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface IGpuDevice {
    ICommandEncoder createCommandEncoder();

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
