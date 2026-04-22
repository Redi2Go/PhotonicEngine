package at.redi2go.photonics.api.gpu.buffers.heap;

import at.redi2go.photonics.api.Disposable;

import java.nio.ByteBuffer;

public interface MemoryView extends Disposable {
    long begin();
    long end();

    default long size() {
        return end() - begin();
    }

    ByteBuffer buffer();

    void upload();

    static int intBufferBegin(MemoryView memory) {
        if (memory == null) throw new IllegalStateException();

        return (int) (memory.begin() >> 2);
    }
}
