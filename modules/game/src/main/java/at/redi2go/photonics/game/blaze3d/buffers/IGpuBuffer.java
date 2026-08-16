package at.redi2go.photonics.game.blaze3d.buffers;

import at.redi2go.photonics.game.Disposable;

import java.nio.ByteBuffer;

public interface IGpuBuffer extends Disposable {
    @BufferUsage int ph$usage();

    long ph$size();

    IGpuBufferSlice ph$slice(long offset, long length);

    IGpuBufferSlice ph$slice();

    boolean ph$isClosed();

    interface MappedView extends Disposable {
        ByteBuffer ph$data();
    }
}
