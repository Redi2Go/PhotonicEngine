package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.block.builder.BlockModelBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockModelTemplate;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldRegistry implements RenderingComponent {
    private static final int OPTIMIZATION_THREAD_COUNT = 1;

    private final WorldAllocator worldAllocator;
    private final PaletteTexture paletteTexture;

    private final ConcurrentLong2ObjectMap<CompletionStage<BlockProvider>> blocks = new ConcurrentLong2ObjectMap<>(16);
    private final ConcurrentHashMap<Object, MemoryOwner<?, ?>> hashedObjectCache = new ConcurrentHashMap<>();
    final Queue<Disposable> freeQueue = new ConcurrentLinkedQueue<>();

    private final ExecutorService optimizationService;

    public WorldRegistry(WorldAllocator worldAllocator, PaletteTexture paletteTexture) {
        this.worldAllocator = worldAllocator;
        this.paletteTexture = paletteTexture;

        AtomicInteger threadCount = new AtomicInteger();
        optimizationService =
                Executors.newFixedThreadPool(
                        OPTIMIZATION_THREAD_COUNT,
                        (r) -> new Thread(r, "Photonics Optimization Thread #" + threadCount.incrementAndGet())
                );
    }

    public WorldAllocator worldAllocator() {
        return worldAllocator;
    }


    // Hashed objects

    @SuppressWarnings("unchecked")
    private <T extends MemoryOwner<U, ?>, K, U> MemoryOwner.ManagedRef<U> cacheObject(
            K key,
            Function<K, T> supplier,
            Consumer<T> allocator
    ) {
        var value = hashedObjectCache.get(key);
        if (value != null) return (MemoryOwner.ManagedRef<U>) value.makeManagedRef();

        var newValue = supplier.apply(key);
        var result = hashedObjectCache.putIfAbsent(newValue, newValue);
        if (result == null) {
            allocator.accept(newValue);
            return newValue.makeManagedRef();
        }

        return (MemoryOwner.ManagedRef<U>) result.makeManagedRef();
    }

    void removeObject(MemoryOwner<?, ?> obj) {
        hashedObjectCache.remove(obj);
    }


    // Allocation methods

    public MemoryOwner.ManagedRef<PaletteObject.Entry> allocatePalette(PaletteEntry entry) {
        entry.computeHashCode();

        return cacheObject(
                entry,
                e -> new PaletteObject(this, e),
                e -> e.allocate(paletteTexture)
        );
    }

    public MemoryOwner.ManagedRef<BlockVoxel> allocateBlockVoxel(long hash, int[] data) {
        return cacheObject(
                new BlockVoxel(this, hash),
                e -> e,
                e -> e.allocate(data)
        );
    }

    public MemoryOwner.ManagedRef<BlockHeader> allocateBlockHeader(
            int[] tint,
            List<MemoryOwner.ManagedRef<PaletteObject.Entry>> palette,
            MemoryOwner.ManagedRef<BlockVoxel> blockVoxel,
            long voxelHash,
            long tintHash
    ) {
        return cacheObject(
                new BlockHeader(this, tint, palette, blockVoxel, voxelHash, tintHash),
                e -> e,
                BlockHeader::allocate
        );
    }

    public CompletionStage<BlockProvider> createBlockModel(BlockBakery.MeshResult blockMesh) {
        return blocks.computeIfAbsent(blockMesh.vertexHash(), (hash) -> {
            var result = new CompletableFuture<BlockProvider>();

            try {
                var builder = new BlockModelBuilder(this, hash, blockMesh.tintData());

                blockMesh.bake(builder);
                result.complete(builder.build());
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }

            return result;
        });
    }

    public void removeBlockModel(long vertexHash) {
        blocks.remove(vertexHash);
    }

    public void scheduleOptimization(Runnable runnable) {
        try {
            optimizationService.execute(runnable);
        } catch (RejectedExecutionException e) {
            // Nothing
        }
    }

    public void freeUnusedBlocks() {
        while (!freeQueue.isEmpty()) {
            var handle = freeQueue.poll();
            if (handle == null) return;

            handle.close();
        }
    }

    @Override
    public void close() {
        optimizationService.shutdownNow();
        worldAllocator.close();
    }
}
