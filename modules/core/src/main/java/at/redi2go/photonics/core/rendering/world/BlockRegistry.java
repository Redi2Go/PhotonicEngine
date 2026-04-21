package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;

public interface BlockRegistry {
    BlockEntry.Builder newBlockBuilder();

    ContainedBlockEntry.Factory newContainedEntry(long vertexHash);

    void freeUnusedBlocks();
}
