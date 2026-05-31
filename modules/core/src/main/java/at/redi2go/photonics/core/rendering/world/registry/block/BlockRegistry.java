package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.registry.object.ObjectRegistry;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import at.redi2go.photonics.core.rendering.world.registry.palete.PaletteRegistry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReadWriteLock;

public class BlockRegistry extends ObjectRegistry<BlockObject> {
    private final WorldAllocator allocator;
    private final PaletteRegistry paletteRegistry;

    public BlockRegistry(
            ReadWriteLock lock,
            WorldAllocator allocator,
            PaletteRegistry paletteRegistry
    ) {
        super(lock);

        this.allocator = allocator;
        this.paletteRegistry = paletteRegistry;
    }

    public @WeakValue VoxelLayer allocateVoxelLayer(@Nullable VoxelTreeEntry[] entries) {
        return (VoxelLayer) cacheObject(
                new VoxelLayer(entries, paletteRegistry, this),
                (e) -> e.allocate(allocator)
        );
    }

    public @WeakValue BlockLayer allocateBlockLayer(
            @Nullable VoxelLayer[] entries,
            int size,
            long hash,
            int boundingVolume
    ) {
        return (BlockLayer) cacheObject(
                new BlockLayer(entries, size, hash, boundingVolume, this),
                (e) -> e.allocate(allocator)
        );
    }
}
