package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;

public class ChunkVoxel extends WorldVoxel {
    public ChunkVoxel(int depth, WorldAllocator allocator) {
        super(depth, allocator);
    }

    @Override
    protected Object entryCreate(int depth) {
        return allocator.createBlockBuilder();
    }

    @Override
    protected boolean entryIsEmpty(Object entry) {
        return false;
    }

    @Override
    protected int entryBegin(Object entry) {
        return ((BlockEntry) entry).begin();
    }

    @Override
    protected Object entryMakeMutable(Object entry) {
        if (entry instanceof BlockEntry.Builder) return entry;

        return ((BlockEntry) entry).createBuilder();
    }

    @Override
    public boolean entryInsert(
            Object entry,
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    ) {
        return ((BlockEntry.Builder) entry).insert(
                x, y, z,
                region,
                normal,
                tint,
                textureData
        );
    }

    @Override
    protected Object entryBuild(Object entry) {
        if (entry instanceof BlockEntry.Builder builder)
            return builder.build();

        return entry;
    }

    @Override
    protected void entryUpload(Object entry) {

    }
}
