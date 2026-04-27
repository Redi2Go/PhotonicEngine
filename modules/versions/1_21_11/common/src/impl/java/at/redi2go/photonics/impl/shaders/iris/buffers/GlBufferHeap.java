package at.redi2go.photonics.impl.shaders.iris.buffers;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.AbstractGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.GlBufferAccessor;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL30C.GL_MAP_INVALIDATE_RANGE_BIT;
import static org.lwjgl.opengl.GL30C.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL45C.glMapNamedBufferRange;
import static org.lwjgl.opengl.GL45C.glUnmapNamedBuffer;

public class GlBufferHeap extends AbstractGpuBufferHeap {
    public static final int NO_PERSISTENCE_MAPPING = 1 << 20;

    private final IGpuBuffer gpuBuffer;
    private final ByteBuffer buffer;

    private final int handle;

    private final Queue<Region> uploadQueue = new ConcurrentLinkedQueue<>();

    public GlBufferHeap(
            IGpuDevice device,
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    ) {
        this.gpuBuffer = device.createBuffer(label, byteSize, usage | BufferUsage.MAP_WRITE | NO_PERSISTENCE_MAPPING);
        this.buffer = ByteBuffer.allocateDirect(Math.toIntExact(byteSize))
                .order(ByteOrder.nativeOrder());

        this.handle = ((GlBufferAccessor) gpuBuffer).getHandle();
    }

    public IGpuBuffer buffer() {
        return gpuBuffer;
    }

    @Override
    public long capacity() {
        return gpuBuffer.size();
    }

    @Nullable
    @Override
    public MemoryView allocate(long byteSize) {
        long ptr = allocatePtr(byteSize);

        return ptr == -1 ? null : new Allocation(ptr, byteSize);
    }

    @Nullable
    @Override
    public IGpuBufferHeap allocateHeap(long byteSize) {
        long ptr = allocatePtr(byteSize);

        return ptr == -1 ? null : new SubHeap(this, ptr, byteSize);
    }

    @Override
    public void upload() {
        var regionsToUpload = Region.takeFrom(uploadQueue);

        for (var region : regionsToUpload) {
            int offset = (int) region.begin();
            int length = (int) region.end() - offset;

            var slice = glMapNamedBufferRange(handle, offset, length, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_RANGE_BIT);
            Objects.requireNonNull(slice, "failed to map buffer range");

            try {
                slice.put(0, buffer, offset, length);
            } finally {
                glUnmapNamedBuffer(handle);
            }
        }
    }

    @Override
    public void close() {
        gpuBuffer.close();
    }

    private class Allocation extends AbstractAllocation {
        private final ByteBuffer slice;

        private Allocation(long begin, long length) {
            super(begin, length);

            slice = buffer.slice(Math.toIntExact(begin), Math.toIntExact(length))
                    .order(buffer.order());
        }

        @Override
        public ByteBuffer buffer() {
            return slice;
        }

        @Override
        public void upload() {
            uploadQueue.add(this);
        }
    }

    private class SubHeap extends AbstractSubheap implements Region, IGpuBufferHeap {
        protected SubHeap(AbstractGpuBufferHeap root, long begin, long length) {
            super(root, begin, length);
        }

        @Override
        protected ByteBuffer createSlice(long begin, long length) {
            return buffer.slice(Math.toIntExact(begin), Math.toIntExact(length))
                    .order(buffer.order());
        }

        @Override
        protected void uploadSlice(Region region) {
            uploadQueue.add(region);
        }

        @Override
        protected IGpuBufferHeap createHeap(long begin, long length) {
            return new SubHeap(GlBufferHeap.this, begin, length);
        }
    }
}