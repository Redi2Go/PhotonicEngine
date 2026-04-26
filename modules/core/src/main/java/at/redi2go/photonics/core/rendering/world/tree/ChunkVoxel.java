package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

public class ChunkVoxel extends WorldVoxel {
    private boolean setPos = false;
    private int x, y, z;

    public ChunkVoxel(
            int depth,
            WorldCompiler compiler,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(depth, compiler, blockRegistry, heap, uploadQueue);
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
        compiler.addChunk(this);
    }

    void updatePos(int x, int y, int z) {
        this.x = x & -16;
        this.y = y & -16;
        this.z = z & -16;
    }

    @Override
    protected VoxelEntry newMutableEntry(int depth) {
        return blockRegistry.newBlockBuilder();
    }

    @Override
    public void insertVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        initPos(x, y, z);

        super.insertVoxel(x, y, z, region, normal, tint, textureData);
    }

    @Override
    public void insertBlock(int x, int y, int z, short region, BlockEntry block) {
        initPos(x, y, z);
        containedRegions.add(region);

        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        final var entry = getEntry(index);
        VoxelEntry newEntry;

        if (entry != null) {
            newEntry = entry.toMutableEntry();
            newEntry.insertBlock(x, y, z, region, block);
        } else newEntry = block;

        setEntry(index, entry, newEntry);
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
    public void removeChunk(int x, int y, int z, short region) {
        throw new UnsupportedOperationException("removeChunk");
    }

    @Override
    public void removeChunkUnsafe(int x, int y, int z) {
        throw new UnsupportedOperationException("removeChunkUnsafe");
    }

    @Override
    public void close() {
        super.close();
        compiler.removeChunk(this);
    }
}
