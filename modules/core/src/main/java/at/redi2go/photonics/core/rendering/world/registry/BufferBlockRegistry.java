package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.BlockVariantBuilder;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.block.BufferBlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.block.BufferBlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.block.regular.RegularBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.variant.BufferBlockVariantBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.variant.BufferBlockVariantFuture;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

public class BufferBlockRegistry implements BlockRegistry {
    private final IGpuBufferHeap heap;
    private final PaletteTexture paletteTexture;

    private final ConcurrentLong2ObjectMap<BlockProvider> blockVariants = new ConcurrentLong2ObjectMap<>();
    private final ConcurrentHashMap<Object, ManagedObject> hashedObjectCache = new ConcurrentHashMap<>();
    private final Set<ManagedObject> freeQueue = ConcurrentHashMap.newKeySet();

    private final ExecutorService optimizationService =
            Executors.newSingleThreadExecutor((r) -> new Thread(r, "Photonics Optimization Thread"));

    public BufferBlockRegistry(
            IGpuBufferHeap heap,
            PaletteTexture paletteTexture
    ) {
        this.heap = heap;
        this.paletteTexture = paletteTexture;
    }

    @Override
    public BlockEntry.Builder newBlockBuilder() {
        return new RegularBlockBuilder(this);
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

    // Hashed objects

        @SuppressWarnings("unchecked")
    private <T extends ManagedObject, K> T cacheObject(
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

    public void freeObject(ManagedObject object) {
        freeQueue.add(object);
    }

    public void freeBlockVariant(long vertexHash) {
        blockVariants.remove(vertexHash);
    }

    @Override
    public void freeUnusedBlocks() {
        for (var obj : freeQueue) {
            if (obj.count() > 0) continue;

            obj.free();
            hashedObjectCache.remove(obj);
        }

        freeQueue.clear();
    }

    @Override
    public void scheduleOptimization(Runnable runnable) {
        try {
            optimizationService.execute(runnable);
        } catch(RejectedExecutionException e) {
            // Nothing
        }
    }

    // Allocation methods

    public PaletteAllocation allocatePalette(PaletteEntry entry) {
        entry.computeHashCode();

        return cacheObject(
                entry,
                e -> new PaletteAllocation(e, this),
                e -> e.allocate(paletteTexture)
        );
    }

    public BufferBlockVoxel allocateBlockVoxel(long hashCode, int[] data) {
        return cacheObject(
                new BufferBlockVoxel(this, hashCode),
                e -> e,
                e -> e.allocate(data)
        );
    }

    public BufferBlockHeader allocateBlockHeader(
            BufferBlockVoxel blockVoxel,
            int skylight,
            PaletteAllocation[] palette,
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

    // Local methods
    public MemoryView allocate(int byteSize) {
        return heap.allocateOrThrow(byteSize);
    }

    @Override
    public void close() {
        optimizationService.shutdownNow();
    }
}
