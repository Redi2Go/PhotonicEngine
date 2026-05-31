package at.redi2go.photonics.core.old.world.block;

import at.redi2go.photonics.core.old.model.VoxelEntry;

public interface BlockEntry extends VoxelEntry {
    int boundingVolume();

    BlockEntry merge(BlockEntry entry);
}
