package at.redi2go.photonics.api.gpu.buffers;

public interface IGpuBufferSlice {
    IGpuBuffer ph$buffer();

    long ph$offset();
    long ph$length();

    IGpuBufferSlice ph$slice(long offset, long length);
}
