package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.core.rendering.world.block.TextureData;

public interface VoxelConsumer {
    void acceptVoxel(
            int x, int y, int z,
            int normal,
            TextureData textureData
    );
}
