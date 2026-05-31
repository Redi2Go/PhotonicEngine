package at.redi2go.photonics.core.rendering.world.registry.block.entry;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.model.BlockPartImpl;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public record SimpleBlockEntry(
        int region,
        BlockPartImpl part
) implements BlockEntry {
    @Override
    public int boundingVolume() {
        return part.blockLayer().boundingVolume();
    }

    @Override
    public int depth() {
        return part.blockLayer().depth();
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        part.blockLayer().uploadTo(memory);
    }

    @Override
    public @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
        return regions.contains(region) ? null : this;
    }

    @Override
    public BlockEntry merge(BlockEntry entry) {
        throw new NotImplementedException("TODO");
    }

    @Override
    public void close() {
        part.close();
    }
}
