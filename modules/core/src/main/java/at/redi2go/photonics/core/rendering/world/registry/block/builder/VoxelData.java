package at.redi2go.photonics.core.rendering.world.registry.block.builder;

import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;

public class VoxelData implements VoxelTreeEntry {
    public int normal;
    public TextureData textureData;

    @Override
    public int depth() {
        return -1;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        throw new UnsupportedOperationException("uploadTo");
    }
}
