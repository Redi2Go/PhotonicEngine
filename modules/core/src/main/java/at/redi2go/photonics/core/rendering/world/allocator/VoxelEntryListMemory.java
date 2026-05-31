package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;

public interface VoxelEntryListMemory extends Disposable {
    int entryData();

    void resize(int newSize);

    VoxelEntryMemory get(int index);

    void upload();
}
