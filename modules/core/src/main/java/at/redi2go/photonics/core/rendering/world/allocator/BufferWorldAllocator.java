package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.world.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BufferWorldAllocator implements WorldAllocator, Disposable {
    private final IGpuBufferHeap heap;
    private final PaletteTexture paletteTexture;

    private final ConcurrentLong2ObjectMap<Object> ownerLookup = new ConcurrentLong2ObjectMap<>(8);
    private final ConcurrentHashMap<PaletteEntry, PaletteAllocation> paletteLookup = new ConcurrentHashMap<>();

    private final Set<RefCounter> freeQueue = ConcurrentHashMap.newKeySet();

    public BufferWorldAllocator(IGpuBufferHeap heap, PaletteTexture allocator) {
        this.heap = heap;
        this.paletteTexture = allocator;
    }

    @Override
    public MemoryView allocate(int byteSize, Object object) {
        MemoryView original = heap.allocateOrThrow(byteSize);
        ownerLookup.put(original.begin(), object);

        return new ObjectMemoryView(original);
    }

    PaletteAllocation allocatePalette(PaletteEntry entry) {
        var value = paletteLookup.get(entry);
        if (value != null)
            return value;

        var palette = new PaletteAllocation(entry, this);

        var result = paletteLookup.putIfAbsent(palette, palette);
        if (result == null) return palette;

        result.allocate(paletteTexture);

        return result;
    }

    @Override
    public BlockEntry.Builder createBlockBuilder() {
        return null;
    }

    @Override
    public @Nullable Object getOwner(int begin) {
        return null;
    }

    public void enqueueFreedObject(RefCounter object) {
        freeQueue.add(object);
    }

    @Override
    public void freeUnusedObjects() {
        for (var obj : freeQueue) {
            if (obj.count() > 0) continue;

            obj.close();

            if (obj instanceof PaletteAllocation)
                paletteLookup.remove(obj);
        }
    }

    @Override
    public void close() {
        heap.close();
        paletteTexture.close();
    }

    private class ObjectMemoryView implements MemoryView {
        private final MemoryView original;

        ObjectMemoryView(MemoryView original) {
            this.original = original;
        }

        @Override
        public long begin() {
            return original.begin();
        }

        @Override
        public long end() {
            return original.end();
        }

        @Override
        public ByteBuffer buffer() {
            return original.buffer();
        }

        @Override
        public void upload() {
            original.upload();
        }

        @Override
        public void close() {
            ownerLookup.remove(original.begin());
            original.close();
        }
    }
}
