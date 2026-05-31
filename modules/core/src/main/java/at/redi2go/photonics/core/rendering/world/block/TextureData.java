package at.redi2go.photonics.core.rendering.world.block;

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

    public TextureData withTint(int tint) {
        return new TextureData(
                blockId,
                VoxelColor.applyTint(color, tint)
        );
    }

    public static int fastEquals(
            TextureData p1,
            TextureData p2
    ) {
        return (p1.blockId ^ p2.blockId) | (p1.color ^ p2.color);
    }
}
