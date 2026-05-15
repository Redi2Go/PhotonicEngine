package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.core.model.VoxelEntry;

public interface BlockEntry extends VoxelEntry {
    int boundingVolume();

    BlockEntry merge(BlockEntry entry);
}
