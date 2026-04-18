package at.redi2go.photonics.core.rendering.world.allocator.palette;

import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTextureView;
import org.joml.Vector4i;

import java.nio.IntBuffer;

public class BufferPaletteAllocator implements PaletteTexture {
    // 4 ints per entry, with 6 faces.
    private static final long ENTRY_BYTE_SIZE = (4 * 4) * 6;

    private final IGpuBufferHeap heap;

    public BufferPaletteAllocator(
            int width,
            int height
    ) {
        heap = IRenderSystem.getDevice().createBufferHeap(
                () -> "Palette Texture",
                width * height * ENTRY_BYTE_SIZE,
                BufferUsage.MAP_WRITE | BufferUsage.COPY_DST
        );
    }

    public IGpuBufferHeap buffer() {
        return heap;
    }

    @Override
    public PaletteTextureView reserveEntry() {
        return new View(heap.allocateOrThrow(ENTRY_BYTE_SIZE));
    }

    @Override
    public void close() {
        heap.close();
    }

    private static class View implements PaletteTextureView {
        private final MemoryView memory;
        private final IntBuffer buffer;

        public View(MemoryView memory) {
            this.memory = memory;
            this.buffer = memory.buffer().asIntBuffer();
        }

        @Override
        public int pos() {
            return (int) memory.begin() >> 4;
        }

        @Override
        public void writeFace(int face, Vector4i value) {
            buffer.position(face << 2);

            buffer.put(value.x);
            buffer.put(value.y);
            buffer.put(value.z);
            buffer.put(value.w);
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
