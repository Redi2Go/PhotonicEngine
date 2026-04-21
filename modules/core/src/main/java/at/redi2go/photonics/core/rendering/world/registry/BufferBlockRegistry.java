package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.block.AbstractBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.contained.ContainedBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.contained.ContainedBlockEntryImpl;
import at.redi2go.photonics.core.rendering.world.registry.block.contained.ContainedBlockFuture;
import at.redi2go.photonics.core.rendering.world.registry.block.contained.ContainedBlockVariant;
import at.redi2go.photonics.core.rendering.world.registry.block.contained.ContainedBlockVoxelImpl;
import at.redi2go.photonics.core.rendering.world.registry.block.regular.RegularBlockBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.regular.RegularBlockVariant;
import at.redi2go.photonics.core.rendering.world.registry.block.regular.RegularBlockVoxel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class BufferBlockRegistry implements BlockRegistry {
    private final IGpuBufferHeap heap;
    private final PaletteTexture paletteTexture;

    private final ConcurrentLong2ObjectMap<ContainedBlockEntry.Factory> containedBlocks = new ConcurrentLong2ObjectMap<>(8);
    private final ConcurrentHashMap<Object, HashedObject> hashedObjectCache = new ConcurrentHashMap<>();
    private final Set<HashedObject> freeQueue = ConcurrentHashMap.newKeySet();

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
    public ContainedBlockEntry.Factory newContainedEntry(long vertexHash) {
        ContainedBlockBuilder[] builderResult = new ContainedBlockBuilder[1];
        var result = containedBlocks.computeIfAbsent(
                vertexHash,
                k -> {
                    var future = new ContainedBlockFuture();
                    var builder = new ContainedBlockBuilder(this, k, future);

                    builderResult[0] = builder;

                    return future;
                }
        );

        return builderResult[0] == null ? result : builderResult[0];
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

    public void freeObject(HashedObject object) {
        freeQueue.add(object);
    }

    public void freeContainedBlock(long vertexHash) {
        containedBlocks.remove(vertexHash);
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

    // Allocation methods

    public PaletteAllocation allocatePalette(PaletteEntry entry) {
        entry.computeHashCode();

        return cacheObject(
                entry,
                e -> new PaletteAllocation(e, this),
                e -> e.allocate(paletteTexture)
        );
    }

    public RegularBlockVoxel allocateRegularBlockVoxel(long hashCode, int shift, int[] data) {
        return cacheObject(
            new RegularBlockVoxel(this, shift, hashCode),
            e -> e,
            e -> e.allocate(data)
        );
    }

    public ContainedBlockVoxelImpl allocateContainedBlockVoxel(
            int shift,
            int valueMask,
            long hashCode,
            long vertexHash,
            int[] data,
            int[] tintMappings,
            PaletteAllocation[] palette
    ) {
        return cacheObject(
                new ContainedBlockVoxelImpl(this, shift, valueMask, hashCode, vertexHash, tintMappings, palette),
                e -> e,
                e -> e.allocate(data)
        );
    }

    public RegularBlockVariant allocateRegularBlockVariant(
            RegularBlockVoxel blockVoxel,
            int shift,
            int valueMask,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint
    ) {
        return cacheObject(
                new RegularBlockVariant(this, blockVoxel, shift, valueMask, skylight, palette, tint),
                e -> e,
                AbstractBlockEntry::allocate
        );
    }

    public ContainedBlockVariant allocateContainedBlockVariant(
            ContainedBlockVoxelImpl blockVoxel,
            int shift,
            int valueMask,
            int skylight,
            PaletteAllocation[] palette,
            int[] tint
    ) {
        return cacheObject(
                new ContainedBlockVariant(this, blockVoxel, shift, valueMask, skylight, palette, tint),
                e -> e,
                AbstractBlockEntry::allocate
        );
    }


    // Local methods
    public MemoryView allocate(int byteSize) {
        return heap.allocateOrThrow(byteSize);
    }
}
