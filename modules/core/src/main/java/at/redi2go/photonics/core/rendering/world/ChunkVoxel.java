package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;

public class ChunkVoxel extends WorldVoxel {
    public ChunkVoxel(int depth, WorldAllocator allocator) {
        super(depth, allocator);
    }

    @Override
    protected Object createVoxel(int depth) {
        return allocator.createBlockBuilder();
    }

    @Override
    protected int getVoxelPtr(Object voxel) {
        return ((BlockEntry) voxel).memory();
    }

    @Override
    protected Object createMutableCopy(Object voxel) {
        return ((BlockEntry) voxel).createBuilder();
    }

    @Override
    protected boolean setVoxelData(
            Object voxel,
            int x, int y, int z,
            int region,
            int normal,
            TextureData textureData
    ) {
        return ((BlockEntry.Builder) voxel).insert(x, y, z, region, normal, textureData);
    }

    @Override
    protected void finalizeVoxel(int index, Object voxel) {
        set(index, ((BlockEntry.Builder) voxel).build().memory());
    }
}
