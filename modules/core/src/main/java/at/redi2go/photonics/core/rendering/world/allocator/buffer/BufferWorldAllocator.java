package at.redi2go.photonics.core.rendering.world.allocator.buffer;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import at.redi2go.photonics.core.iris.pipeline.buffer.IBufferHolder;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.allocator.WorldLightMemory;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
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
    public VoxelEntryMemory allocateEntry(boolean useChildMask, int extraFields) {
        return new EntryMemoryImpl(
                heap.allocateOrThrow(entryByteSize(useChildMask, extraFields)),
                useChildMask,
                extraFields
        );
    }

    @Override
    public VoxelEntryListMemory allocateEntryList(boolean useChildMask, int extraFields) {
        return new EntryListImpl(useChildMask, extraFields);
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

    private static abstract class AbstractEntryMemory implements VoxelEntryMemory {
        protected abstract boolean hasChildMask();

        protected abstract int extraFieldCount();

        protected abstract ByteBuffer buffer();

        protected abstract int offset();

        @Override
        public void setEntryFlag(boolean isLeaf) {
            var buffer = buffer();
            int offset = offset();

            int data0 = buffer.getInt(offset);
            buffer.putInt(
                    offset,
                    (data0 & ~1) | (isLeaf ? 1 : 0)
            );
        }

        @Override
        public void setEntryData(int entryData) {
            if (entryData < 0) throw new IllegalArgumentException("data must positive");

            var buffer = buffer();
            int offset = offset();

            int data0 = buffer.getInt(offset);
            buffer.putInt(
                    offset,
                    (data0 & 1) | entryData << 1
            );
        }

        @Override
        public void setChildMask(long mask) {
            if (!hasChildMask())
                throw new IllegalArgumentException("does not support child mask");

            int offset = offset();

            buffer().putInt(offset + 4, (int) mask);
            buffer().putInt(offset + 8, (int) (mask >>> 32));
        }

        @Override
        public void setExtraFields(int... extra) {
            if (extra.length > extraFieldCount())
                Objects.checkIndex(extra.length - 1, extraFieldCount());

            var buffer = buffer();
            int baseOffset = offset() + 12;
            for (int i = 0; i < extra.length; i++)
                buffer.putInt(baseOffset + (i << 2), extra[i]);
        }
    }

    private static class EntryMemoryImpl extends AbstractEntryMemory {
        private final MemoryView memory;
        private final boolean hasChildMask;
        private final int extraFieldCount;

        private EntryMemoryImpl(
                MemoryView memory,
                boolean hasChildMask,
                int extraFieldCount
        ) {
            this.memory = memory;
            this.hasChildMask = hasChildMask;
            this.extraFieldCount = extraFieldCount;
        }

        @Override
        protected boolean hasChildMask() {
            return hasChildMask;
        }

        @Override
        protected int extraFieldCount() {
            return extraFieldCount;
        }

        @Override
        protected ByteBuffer buffer() {
            return memory.buffer();
        }

        @Override
        protected int offset() {
            return 0;
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

    private class EntryListImpl implements VoxelEntryListMemory {
        private final boolean hasChildMask;
        private final int extraFieldCount;

        private MemoryView memory = null;
        private int capacity = -1;

        private EntryListImpl(boolean hasChildMask, int extraFieldCount) {
            this.hasChildMask = hasChildMask;
            this.extraFieldCount = extraFieldCount;
        }

        @Override
        public int entryData() {
            var memory = this.memory;
            return memory == null ? 0 : MemoryView.intBufferBegin(memory);
        }

        @Override
        public void resize(int newSize) {
            int newCapacity = capacity(newSize, capacity);
            if (capacity == newCapacity) return;

            if (memory != null) memory.close();

            memory = heap.allocateOrThrow((long) entryByteSize(hasChildMask, extraFieldCount) * newCapacity);
            capacity = newCapacity;
        }

        @Override
        public VoxelEntryMemory get(int index) {
            Objects.checkIndex(index, capacity);
            return new Entry(entryByteSize(hasChildMask, extraFieldCount) * index);
        }

        @Override
        public void upload() {
            if (memory != null)
                memory.upload();
        }

        @Override
        public void close() {
            if (memory != null)
                memory.close();
        }

        private static int capacityTarget(int size) {
            if (size <= 8) return 8;
            if (size <= 16) return 16;
            if (size <= 24) return 24;
            if (size <= 32) return 32;
            if (size <= 40) return 40;
            if (size <= 48) return 48;
            if (size <= 56) return 56;

            return 64;
        }

        private static int capacity(int size, int currentCapacity) {
            int idealCapacity = capacityTarget(size);

            if (currentCapacity == -1) return idealCapacity;
            if (idealCapacity == currentCapacity) return idealCapacity;
            if (idealCapacity > currentCapacity) return idealCapacity;

            int diff = currentCapacity - idealCapacity;
            if (diff > 8) return idealCapacity;

            return currentCapacity;
        }

        private class Entry extends AbstractEntryMemory {
            private final int offset;

            public Entry(int offset) {
                this.offset = offset;
            }

            @Override
            protected boolean hasChildMask() {
                return hasChildMask;
            }

            @Override
            protected int extraFieldCount() {
                return extraFieldCount;
            }

            @Override
            protected ByteBuffer buffer() {
                return memory.buffer();
            }

            @Override
            protected int offset() {
                return offset;
            }

            @Override
            public void upload() {
                memory.upload();
            }

            @Override
            public void close() {

            }
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

    private static int entryByteSize(boolean useChildMask, int extraFields) {
        return 4 + (useChildMask ? 8 : 0) + (extraFields << 4);
    }
}
