package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;

public interface VoxelConsumer {
    void accept(
            int x,
            int y,
            int z,
            int normal,
            TextureData textureData
    );
}
