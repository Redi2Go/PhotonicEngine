package at.redi2go.photonics.core.old.world.allocator;

import at.redi2go.photonics.api.Disposable;

public interface WorldVoxelMemory extends Disposable {
    int entryData();

    int getEntry(int index);

    void setEntry(int index, int entry);

    void setData(int[] voxelData);

    void upload();
}
