package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;

import java.util.Queue;

public class ChunkVoxel extends WorldVoxel {
    private boolean setPos = false;
    private int x, y, z;

    protected ChunkVoxel(
            int depth,
            BlockMergeMode mergeMode,
            ChunkManager chunkManager,
            WorldRegistry worldRegistry,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(depth, mergeMode, chunkManager, worldRegistry, uploadQueue);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    protected void initPos(int x, int y, int z) {
        if (setPos) return;

        setPos = true;
        updatePos(x, y, z);
        chunkManager.addChunk(this);
    }

    void updatePos(int x, int y, int z) {
        this.x = x & -16;
        this.y = y & -16;
        this.z = z & -16;
    }

    @Override
    protected VoxelEntry newMutableEntry(int depth) {
        throw new UnsupportedOperationException("newMutableEntry");
    }

    @Override
    public void insertBlock(int x, int y, int z, int region, BlockEntry block) {
        initPos(x, y, z);
        containedRegions.add(region);

        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());

        final VoxelEntry oldEntry = getEntry(index);
        final VoxelEntry newEntry = oldEntry == null ? block : mergeMode.merge((BlockEntry) oldEntry, block);

        setEntry(index, oldEntry, newEntry);
    }

    @Override
    public void pruneEmptyVoxels() {
        // Nothing to prune
    }

    @Override
    public void insertChunk(int x, int y, int z, ChunkVoxel chunk) {
        throw new UnsupportedOperationException("insertChunk");
    }

    @Override
    public void removeChunkUnsafe(int x, int y, int z) {
        throw new UnsupportedOperationException("removeChunkUnsafe");
    }

    @Override
    public void close() {
        super.close();
        chunkManager.removeChunk(this);
    }
}
