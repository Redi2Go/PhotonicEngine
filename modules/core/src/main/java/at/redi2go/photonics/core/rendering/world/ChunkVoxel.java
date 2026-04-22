package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;

import java.util.Queue;

public class ChunkVoxel extends WorldVoxel {
    public ChunkVoxel(
            int depth,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(depth, blockRegistry, heap, uploadQueue);
    }

    @Override
    protected VoxelEntry newMutableEntry(int depth) {
        return blockRegistry.newBlockBuilder();
    }

    @Override
    public void insertBlock(int x, int y, int z, short region, BlockEntry block) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = voxelData[index];

        if (entry == null) {
            voxelData[index] = block;
            incVoxelCount();

            requestUpload();
        } else if (entry != block) {
            entry.close();

            voxelData[index] = block;
            requestUpload();
        }
    }
}
