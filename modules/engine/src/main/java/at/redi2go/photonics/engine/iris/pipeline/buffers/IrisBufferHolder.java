package at.redi2go.photonics.engine.iris.pipeline.buffers;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;

import java.util.function.Supplier;

public interface IrisBufferHolder {
    void addDefaultBuffer(String name, Supplier<IGpuBuffer> buffer);
}
