package at.redi2go.photonics.api.gpu.buffers;

import at.redi2go.photonics.api.Disposable;

import java.nio.ByteBuffer;

public interface IGpuBuffer extends Disposable {
    long ph$size();

    @BufferUsage int ph$usage();

    boolean ph$isClosed();

    IGpuBufferSlice ph$slice(long offset, long length);

    interface MappedView extends Disposable {
        ByteBuffer ph$data();
    }
}
