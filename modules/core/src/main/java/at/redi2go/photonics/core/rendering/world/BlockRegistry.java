package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;

public interface BlockRegistry extends Disposable {
    BlockEntry.Builder newBlockBuilder();

    BlockProvider getBlockProvider(long vertexHash);

    void freeUnusedBlocks();

    void scheduleOptimization(Runnable runnable);
}
