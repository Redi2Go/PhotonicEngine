package at.redi2go.photonics.api.gpu.buffers;

public interface IGpuBufferSlice {
    IGpuBuffer buffer();

    long offset();
    long length();

    IGpuBufferSlice slice(long offset, long length);
}
