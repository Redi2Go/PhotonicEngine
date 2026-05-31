package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.core.old.world.allocator.BlockHeaderMemory;
import at.redi2go.photonics.core.old.world.allocator.BlockVoxelMemory;
import at.redi2go.photonics.core.old.world.allocator.WorldVoxelMemory;
import at.redi2go.photonics.core.rendering.RenderingComponent;

public interface WorldAllocator extends RenderingComponent {
    VoxelEntryMemory allocateEntry(boolean useChildMask, int extra);

    VoxelEntryListMemory allocateEntryList(boolean useChildMask, int extraFields);

    WorldLightMemory allocateWorldLight();

    void upload();
}
