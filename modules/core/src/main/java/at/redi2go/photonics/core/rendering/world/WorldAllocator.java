package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;

public interface WorldAllocator extends Disposable {
    MemoryView allocate(int byteSize);

    BlockEntry.Builder createBlockBuilder();

    void freeUnusedObjects();
}
