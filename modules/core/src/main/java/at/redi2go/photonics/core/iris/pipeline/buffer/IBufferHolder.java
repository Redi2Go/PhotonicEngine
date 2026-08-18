package at.redi2go.photonics.core.iris.pipeline.buffer;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;

import java.util.function.Supplier;

public interface IBufferHolder {
    void addDefaultBuffer(String name, Supplier<IGpuBuffer> buffer);
}
