package at.redi2go.photonics.core.rendering.world.registry.block.entry;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockPartImpl;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public record SimpleBlockEntry(
        int region,
        BlockPartImpl part
) implements BlockEntry {
    @Override
    public int entryData() {
        return part.entryData();
    }

    @Override
    public void insertBlock(int x, int y, int z, int region, BlockEntry block) {
        throw new UnsupportedOperationException("insertBlock");
    }

    @Override
    public int boundingVolume() {
        return part.boundingVolume();
    }

    @Override
    public BlockEntry merge(BlockEntry entry) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public @Nullable VoxelEntry removeRegions(IntSet regions) {
        return !regions.contains(region) ? this : null;
    }

    @Override
    public @Nullable VoxelEntry build() {
        return this;
    }

    @Override
    public void close() {
        part.close();
    }
}
