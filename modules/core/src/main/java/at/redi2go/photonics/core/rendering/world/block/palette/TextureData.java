package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import org.joml.Vector4f;

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

    public TextureData withTint(Vector4f tint) {
        var color4 = VoxelColor.toVector(color);

        return new TextureData(
                blockId,
                VoxelColor.fromVector(color4.mul(tint))
        );
    }
}
