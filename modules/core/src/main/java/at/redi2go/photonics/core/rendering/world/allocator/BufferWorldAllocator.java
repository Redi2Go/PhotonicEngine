package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.BufferUsage;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.core.collect.ConcurrentInt2ObjectMap;
import at.redi2go.photonics.core.rendering.world.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.allocator.block.BlockEntryBuilderImpl;
import at.redi2go.photonics.core.rendering.world.allocator.block.BlockEntryData;
import at.redi2go.photonics.core.rendering.world.allocator.block.BlockVoxelImpl;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class BufferWorldAllocator implements WorldAllocator, Disposable {
    private final IGpuBufferHeap heap;
    private final PaletteTexture paletteTexture;

    private final ConcurrentHashMap<Object, HashedObject> hashedObjectCache = new ConcurrentHashMap<>();

    private final Set<HashedObject> freeQueue = ConcurrentHashMap.newKeySet();
    private final Random random = new Random();

    public BufferWorldAllocator(
            long byteSize,
            PaletteTexture allocator
    ) {
        this.heap = IRenderSystem.getDevice()
                .createBufferHeap(
                        () -> "World Buffer",
                        byteSize,
                        BufferUsage.MAP_WRITE
                );

        this.paletteTexture = allocator;
    }

    public IGpuBufferHeap buffer() {
        return heap;
    }

    public PaletteTexture paletteTexture() {
        return paletteTexture;
    }

    @Override
    public MemoryView allocate(int byteSize) {
        return heap.allocateOrThrow(byteSize);
    }

    @Override
    public BlockEntry.Builder createBlockBuilder() {
        return new BlockEntryBuilderImpl(this);
    }


    // Hashed objects

    @SuppressWarnings("unchecked")
    private <T extends HashedObject, K> T cacheObject(
            K key,
            Function<K, T> supplier,
            Consumer<T> allocator
    ) {
        var value = hashedObjectCache.get(key);
        if (value != null) return (T) value;

        var newValue = supplier.apply(key);
        var result = hashedObjectCache.putIfAbsent(newValue, newValue);
        if (result == null) {
            allocator.accept(newValue);
            return newValue;
        }

        return (T) result;
    }

    public void freeHashedObject(HashedObject obj) {
        freeQueue.add(obj);
    }

    // Hash object allocations

    public PaletteAllocation allocatePalette(PaletteEntry entry) {
        return cacheObject(
                entry,
                e -> new PaletteAllocation(e, this),
                e -> e.allocate(paletteTexture)
        );
    }

    public BlockVoxelImpl allocateBlockVoxel(
            long hashCode,
            int shift,
            int[] data
    ) {
        return cacheObject(
            new BlockVoxelImpl(this, shift, hashCode),
            e -> e,
            e -> e.allocate(data)
        );
    }

    public BlockEntryData allocateBlockEntryData(
            int skylight,
            PaletteAllocation[] palette,
            BlockVoxelImpl blockVoxel
    ) {
        return cacheObject(
                new BlockEntryData(this, skylight, palette, blockVoxel),
                e -> e,
                BlockEntryData::allocate
        );
    }

    @Override
    public void freeUnusedObjects() {
        for (var obj : freeQueue) {
            if (obj.count() > 0) continue;

            obj.free();
            hashedObjectCache.remove(obj);
        }

        freeQueue.clear();
    }

    @Override
    public void close() {
        heap.close();
        paletteTexture.close();
    }
}
