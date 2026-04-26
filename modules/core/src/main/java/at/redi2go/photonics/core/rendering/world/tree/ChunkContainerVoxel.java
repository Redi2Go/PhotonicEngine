package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;

import java.util.Queue;

public class ChunkContainerVoxel extends WorldVoxel {
    public ChunkContainerVoxel(
            int depth,
            WorldCompiler compiler,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(depth, compiler, blockRegistry, heap, uploadQueue);
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
    public void removeChunk(int x, int y, int z, short region) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        final var entry = getEntry(index);

        setEntry(index, entry, null);
        containedRegions.remove(region);
    }

    @Override
    public void removeChunkUnsafe(int x, int y, int z) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        final var entry = getEntry(index);

        setEntryUnsafe(index, entry, null);
        containedRegions.clear();
    }
}
