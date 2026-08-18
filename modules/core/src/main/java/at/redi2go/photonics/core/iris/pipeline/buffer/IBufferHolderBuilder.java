package at.redi2go.photonics.core.iris.pipeline.buffer;

import at.redi2go.photonics.game.blaze3d.buffers.IGpuBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IBufferHolderBuilder<T> {
    T withBuffer(Consumer<IBufferHolder> consumer);

    default T buffer(String name, Supplier<IGpuBuffer> buffer) {
        return withBuffer(buffers -> buffers.addDefaultBuffer(name, buffer));
    }
}
