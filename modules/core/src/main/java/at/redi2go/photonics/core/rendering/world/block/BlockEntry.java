package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;

public interface BlockEntry extends VoxelTreeEntry, Disposable {
    int boundingVolume();

    BlockEntry merge(BlockEntry entry);
}
