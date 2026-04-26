package at.redi2go.photonics.api.gpu.buffers.heap;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class DefaultGpuBufferHeap extends AbstractGpuBufferHeap {
    private final IGpuBuffer gpuBuffer;
    private final ByteBuffer buffer;

    private final Queue<Region> uploadQueue = new ConcurrentLinkedQueue<>();

    public DefaultGpuBufferHeap(
            IGpuDevice device,
            @Nullable Supplier<String> label,
            long byteSize,
            @BufferUsage int usage
    ) {
        this.gpuBuffer = device.createBuffer(label, byteSize, usage);
        this.buffer = ByteBuffer.allocateDirect(Math.toIntExact(byteSize))
                .order(ByteOrder.nativeOrder());
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

    private List<? extends MemorySlice> pollUploadQueue() {
        var regions = new ArrayList<Region>();

        while (!uploadQueue.isEmpty()) {
            @Nullable Region region = uploadQueue.poll();
            if (region == null) break;

            regions.add(region);
        }

        return MemorySlice.mergeNeighbors(regions);
    }

    @Override
    public void upload() {
        var regionsToUpload = pollUploadQueue();
        ICommandEncoder encoder = IRenderSystem.getDevice().createCommandEncoder();

        for (var region : regionsToUpload) {
            var slice = gpuBuffer.slice(region.begin(), region.length());
            try (var mappedView = encoder.mapBuffer(slice, false, true)) {
                mappedView.data().put(
                        0,
                        buffer,
                        Math.toIntExact(slice.offset()),
                        Math.toIntExact(slice.length())
                );
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
            return new SubHeap(DefaultGpuBufferHeap.this, begin, length);
        }
    }
}
