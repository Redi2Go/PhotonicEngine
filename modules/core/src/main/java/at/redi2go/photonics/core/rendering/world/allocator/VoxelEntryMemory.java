package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;

public interface VoxelEntryMemory extends Disposable {
    void setEntryFlag(boolean flag);

    void setEntryData(int entryData);

    void setChildMask(long mask);

    void setExtraFields(int... extra);

    void upload();
}
