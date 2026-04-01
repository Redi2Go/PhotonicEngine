package at.redi2go.photonics.api.gpu.buffers;

import at.redi2go.photonics.api.Disposable;

import java.nio.ByteBuffer;

public interface IGpuBuffer extends Disposable {
    long size();
    @BufferUsage int usage();

    boolean isClosed();

    IGpuBufferSlice slice(long offset, long length);

    interface MappedView extends Disposable {
        ByteBuffer data();
    }
}
