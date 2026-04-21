package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;

public interface BlockConsumer {
    void acceptBlock(
            int x, int y, int z,
            short region,
            BlockEntry entry
    );
}
