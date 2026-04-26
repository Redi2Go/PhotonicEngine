package at.redi2go.photonics.core.rendering.world.registry.buffer;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.regular.RegularBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.variant.BufferBlockVariantBuilder;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.variant.BufferBlockVariantFuture;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class BufferBlockRegistry implements BlockRegistry {
    private final IGpuBufferHeap heap;
    private final PaletteTexture paletteTexture;

    private final ConcurrentLong2ObjectMap<BlockProvider> blockVariants = new ConcurrentLong2ObjectMap<>();
    private final ConcurrentHashMap<Object, BufferObject<?, ?>> hashedObjectCache = new ConcurrentHashMap<>();
    final Queue<Disposable> freeQueue = new ConcurrentLinkedQueue<>();

    private final ExecutorService optimizationService =
            Executors.newSingleThreadExecutor((r) -> new Thread(r, "Photonics Optimization Thread"));

    public BufferBlockRegistry(
            IGpuBufferHeap heap,
            PaletteTexture paletteTexture
    ) {
        this.heap = heap;
        this.paletteTexture = paletteTexture;
    }

    public int size() {
        return hashedObjectCache.size();
    }

    // Hashed objects

    @SuppressWarnings("unchecked")
    private <T extends BufferObject<U, ?>, K, U> BufferObject.ManagedRef<U> cacheObject(
            K key,
            Function<K, T> supplier,
            Consumer<T> allocator
    ) {
        var value = hashedObjectCache.get(key);
        if (value != null) return (BufferObject.ManagedRef<U>) value.makeManagedRef();

        var newValue = supplier.apply(key);
        var result = hashedObjectCache.putIfAbsent(newValue, newValue);
        if (result == null) {
            allocator.accept(newValue);
            return newValue.makeManagedRef();
        }

        return (BufferObject.ManagedRef<U>) result.makeManagedRef();
    }

    void removeObject(BufferObject<?, ?> obj) {
        hashedObjectCache.remove(obj);
    }

    public void removeBlockVariant(long vertexHash) {
        blockVariants.remove(vertexHash);
    }

    @Override
    public BlockProvider getBlockProvider(long vertexHash) {
        BufferBlockVariantBuilder[] builderResult = new BufferBlockVariantBuilder[1];
        var result = blockVariants.computeIfAbsent(vertexHash, k -> {
            var future = new BufferBlockVariantFuture();
            var builder = new BufferBlockVariantBuilder(this, k, future);

            builderResult[0] = builder;

            return future;
        });

        return builderResult[0] == null ? result : builderResult[0];
    }

    @Override
    public BlockEntry.Builder newBlockBuilder() {
        return new RegularBlockBuilder(this);
    }

    @Override
    public void freeUnusedBlocks() {
        while (!freeQueue.isEmpty()) {
            var handle = freeQueue.poll();
            if (handle == null) return;

            handle.close();
        }
    }

    @Override
    public void scheduleOptimization(Runnable runnable) {
        try {
            optimizationService.execute(runnable);
        } catch (RejectedExecutionException e) {
            // Nothing
        }
    }

    // Allocation methods

    public BufferObject.ManagedRef<BufferPaletteObject.Entry> allocatePalette(PaletteEntry entry) {
        entry.computeHashCode();

        return cacheObject(
                entry,
                e -> new BufferPaletteObject(e, this),
                e -> e.allocate(paletteTexture)
        );
    }

    public BufferObject.ManagedRef<BufferBlockVoxel> allocateBlockVoxel(long hashCode, int[] data) {
        return cacheObject(
                new BufferBlockVoxel(this, hashCode),
                e -> e,
                e -> e.allocate(data)
        );
    }

    public BufferObject.ManagedRef<BufferBlockHeader> allocateBlockHeader(
            BufferObject.ManagedRef<BufferBlockVoxel> blockVoxel,
            int skylight,
            List<BufferObject.ManagedRef<BufferPaletteObject.Entry>> palette,
            int[] tint,
            long voxelHash,
            long tintHash
    ) {
        return cacheObject(
                new BufferBlockHeader(this, blockVoxel, skylight, palette, tint, voxelHash, tintHash),
                e -> e,
                BufferBlockHeader::allocate
        );
    }

    public MemoryView allocate(int byteSize) {
        return heap.allocateOrThrow(byteSize);
    }


    @Override
    public void close() {
        optimizationService.shutdownNow();
    }
}
