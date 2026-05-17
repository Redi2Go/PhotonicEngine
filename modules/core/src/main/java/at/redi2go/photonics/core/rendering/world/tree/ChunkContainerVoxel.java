package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;

import java.util.Queue;

public class ChunkContainerVoxel extends WorldVoxel {
    public ChunkContainerVoxel(
            int depth,
            BlockMergeMode mergeMode,
            ChunkManager chunkManager,
            WorldRegistry worldRegistry,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(depth, mergeMode, chunkManager, worldRegistry, uploadQueue);
    }

    @Override
    public void insertChunk(int x, int y, int z, ChunkVoxel chunk) {
        int index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        final var entry = getEntry(index);

        if (entry == chunk) return;
        if (entry != null) throw new IllegalArgumentException("Duplicate chunk");

        setEntry(index, null, chunk);
        chunk.updatePos(x, y, z);

        containedRegions.addAll(chunk.containedRegions);
    }

    @Override
    public void removeChunkUnsafe(int x, int y, int z) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        final var entry = getEntry(index);

        setEntryUnsafe(index, entry, null);
        containedRegions.clear();
    }
}
