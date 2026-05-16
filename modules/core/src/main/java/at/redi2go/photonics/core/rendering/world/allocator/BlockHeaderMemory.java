package at.redi2go.photonics.core.rendering.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;

public interface BlockHeaderMemory extends Disposable {
    int entryData();

    void setBlockVoxel(BlockVoxel blockVoxel);

    void setPaletteEntry(int index, int tint, PaletteObject paletteEntry);

    void upload();
}
