package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import it.unimi.dsi.fastutil.ints.IntSet;

public interface BlockEntry extends VoxelTreeEntry, Disposable {
    @Override
    default int depth() {
        return BLOCK_DEPTH;
    }

    int boundingVolume();

    IntSet regions();

    BlockEntry merge(BlockEntry entry);
}
