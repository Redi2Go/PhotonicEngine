package at.redi2go.photonics.core.rendering.world.bakery.texture;

import at.redi2go.photonics.core.rendering.world.block.TextureData;

public interface CpuTexture {
    TextureData sample(
            int blockId,
            float u,
            float v
    );

    @FunctionalInterface
    interface Factory {
        CpuTexture create(int width, int height, int[] data);
    }
}
