package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.world.block.VoxelColor;

public record TextureData(
        int blockId,
        int color
//        for future use
//        int normal,
//        int specular
) {
    public boolean gt(TextureData other) {
        return VoxelColor.gt(color, other.color());
    }
}
