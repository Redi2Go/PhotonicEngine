package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.core.rendering.RenderingComponent;

public interface WorldAllocator extends RenderingComponent {
    WorldVoxelMemory allocateWorldVoxel();

    BlockVoxelMemory allocateBlockVoxel();

    BlockHeaderMemory allocateBlockHeader(int paletteSize);

    WorldLightMemory allocateWorldLight();

    void upload();
}
