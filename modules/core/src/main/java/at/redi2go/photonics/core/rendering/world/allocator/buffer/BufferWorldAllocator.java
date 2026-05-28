package at.redi2go.photonics.core.rendering.world.allocator.buffer;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.allocator.BlockHeaderMemory;
import at.redi2go.photonics.core.rendering.world.allocator.BlockVoxelMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.allocator.WorldLightMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldVoxelMemory;
import at.redi2go.photonics.core.rendering.world.registry.BlockLightOwner;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.IntBuffer;
import java.util.Objects;

public class BufferWorldAllocator implements WorldAllocator {
    private final IGpuBufferHeap heap;

    public BufferWorldAllocator(long byteSize) {
        this.heap = IRenderSystem.getDevice().ph$createBufferHeap(
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
                heap.allocateOrThrow((long) (adjustedLength + 3) << 2),
                paletteSize
        );
    }

    @Override
    public WorldLightMemory allocateWorldLight() {
        return new WorldLightAllocation(heap.allocateOrThrow(10 << 2));
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
        private static final int BLOCK_VOXEL_OFFSET = 0;
        private static final int LIGHT_OFFSET = BLOCK_VOXEL_OFFSET + 4;
        private static final int PALETTE_ENTRIES_OFFSET = LIGHT_OFFSET + 4;

        @Override
        public int entryData() {
            return MemoryView.intBufferBegin(memory);
        }

        @Override
        public void setBlockVoxel(BlockVoxel blockVoxel) {
            memory.buffer().putInt(BLOCK_VOXEL_OFFSET, blockVoxel.entryData());
        }

        @Override
        public void setLight(@Nullable BlockLightOwner light) {
            memory.buffer().putInt(LIGHT_OFFSET, light != null ? light.entryData() : 0);
        }

        @Override
        public void setPaletteEntry(int index, int tint, PaletteObject paletteEntry) {
            Objects.checkIndex(index, paletteSize);

            var buffer = memory.buffer();
            int offset = PALETTE_ENTRIES_OFFSET + (index << 3);

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

    private record WorldLightAllocation(MemoryView memory) implements WorldLightMemory {

        @Override
        public int entryData() {
            return MemoryView.intBufferBegin(memory);
        }

        @Override
        public void setLight(BlockLightInfo light, int blockId) {
            var data = light.toVector4Array(new Vector3f(0f), blockId);
            var buffer = memory.buffer().asIntBuffer();

            buffer.put(0, light.isTraced() ? 2 : 1);
            buffer.put(1, Float.floatToRawIntBits(data[0].w));

            putVec4(buffer, 2, data[1]);
            putVec4(buffer, 6, data[2]);
        }

        @Override
        public void upload() {
            memory.upload();
        }

        @Override
        public void close() {
            memory.close();
        }

        private static void putVec4(IntBuffer buffer, int offset, Vector4f vec) {
            buffer.put(offset, Float.floatToRawIntBits(vec.x));
            buffer.put(offset + 1, Float.floatToRawIntBits(vec.y));
            buffer.put(offset + 2, Float.floatToRawIntBits(vec.z));
            buffer.put(offset + 3, Float.floatToRawIntBits(vec.w));
        }
    }
}
