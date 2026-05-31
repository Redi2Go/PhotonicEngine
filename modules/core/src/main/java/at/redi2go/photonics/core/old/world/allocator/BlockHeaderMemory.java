package at.redi2go.photonics.core.old.world.allocator;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.registry.palete.LivePaletteEntry;
import at.redi2go.photonics.core.rendering.world.registry.light.WorldLight;
import at.redi2go.photonics.core.old.world.registry.block.BlockVoxel;
import org.jetbrains.annotations.Nullable;

public interface BlockHeaderMemory extends Disposable {
    int entryData();

    void setBlockVoxel(BlockVoxel blockVoxel);

    void setLight(@Nullable WorldLight light);

    void setPaletteEntry(int index, int tint, LivePaletteEntry paletteEntry);

    void upload();
}
