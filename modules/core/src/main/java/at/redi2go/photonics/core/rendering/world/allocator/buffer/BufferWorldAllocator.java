package at.redi2go.photonics.core.rendering.world.allocator.buffer;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.allocator.BlockHeaderMemory;
import at.redi2go.photonics.core.rendering.world.allocator.BlockVoxelMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.allocator.WorldVoxelMemory;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;

import java.util.Objects;

public class BufferWorldAllocator implements WorldAllocator {
    private final IGpuBufferHeap heap;

    public BufferWorldAllocator(long byteSize) {
        this.heap = IRenderSystem.getDevice().createBufferHeap(
                () -> "Photonics World Buffer",
                byteSize,
                0
        );
    }

    @Override
    public WorldVoxelMemory allocateWorldVoxel() {
        return new VoxelAllocation(heap.allocateOrThrow(RtVoxel.ENTRIES_SIZE << 2));
    }

    @Override
    public BlockVoxelMemory allocateBlockVoxel() {
        return new VoxelAllocation(heap.allocateOrThrow(RtVoxel.ENTRIES_SIZE << 2));
    }

    @Override
    public BlockHeaderMemory allocateBlockHeader(int paletteSize) {
        int adjustedLength = Integer.highestOneBit(paletteSize);
        if (adjustedLength != paletteSize) adjustedLength <<= 1;

        adjustedLength <<= 1;

        return new BlockHeaderAllocation(
                heap.allocateOrThrow((long) (adjustedLength + 2) << 2),
                paletteSize
        );
    }

    @Override
    public void registerBuffers(IBufferHolder buffers) {
        buffers.addDefaultBufferHeap("ph_world_voxel_buffer", () -> heap);
    }

    @Override
    public void upload() {
        heap.upload();
    }

    @Override
    public void close() {
        heap.close();
    }

    private record VoxelAllocation(MemoryView memory) implements WorldVoxelMemory, BlockVoxelMemory {
        @Override
        public int entryData() {
            return MemoryView.intBufferBegin(memory());
        }

        @Override
        public int getEntry(int index) {
            return memory.buffer().getInt(index << 2);
        }

        @Override
        public void setEntry(int index, int entry) {
            memory.buffer().putInt(index << 2, entry);
        }

        @Override
        public void setData(int[] voxelData) {
            memory.buffer().asIntBuffer()
                    .put(0, voxelData);
        }

        @Override
        public void upload() {
            memory.upload();
        }

        @Override
        public void close() {
            memory.close();
        }
    }

    private record BlockHeaderAllocation(MemoryView memory, int paletteSize) implements BlockHeaderMemory {
        BlockHeaderAllocation {
            memory.buffer().putInt(4, paletteSize);
        }

        @Override
        public int entryData() {
            return MemoryView.intBufferBegin(memory);
        }

        @Override
        public void setBlockVoxel(BlockVoxel blockVoxel) {
            memory.buffer().putInt(0, blockVoxel.entryData());
        }

        @Override
        public void setPaletteEntry(int index, int tint, PaletteObject paletteEntry) {
            Objects.checkIndex(index, paletteSize);

            var buffer = memory.buffer();
            int offset = 8 + (index << 3);

            buffer.putInt(offset, tint);
            buffer.putInt(offset + 4, paletteEntry.entryData());
        }

        @Override
        public void upload() {
            memory.upload();
        }

        @Override
        public void close() {
            memory.close();
        }
    }
}
