package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public interface BlockEntry extends VoxelEntry {
    int skylight();

    interface Builder extends BlockEntry {
        void setSkylight(int skylight);

        @Nullable BlockEntry build();
    }
}
