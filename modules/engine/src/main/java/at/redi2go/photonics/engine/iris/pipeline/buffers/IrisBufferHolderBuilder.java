package at.redi2go.photonics.engine.iris.pipeline.buffers;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IrisBufferHolderBuilder<T> {
    T withBuffer(Consumer<IrisBufferHolder> consumer);

    default T buffer(String name, Supplier<IGpuBuffer> buffer) {
        return withBuffer(buffers -> buffers.addDefaultBuffer(name, buffer));
    }
}
