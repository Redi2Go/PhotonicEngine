package at.redi2go.photonics.core.rendering.world.block;

public record TextureData(
        int blockId,
        int color,
        int normal,
        int specular
) {
    public static final int DEFAULT_NORMAL = 2139095039;
    public static final int DEFAULT_SPECULAR = 0;

    public boolean gt(TextureData other) {
        return VoxelColor.gt(color, other.color());
    }

    public TextureData withTint(int tint) {
        return new TextureData(
                blockId,
                VoxelColor.applyTint(color, tint),
                normal,
                specular
        );
    }

    public static int fastEquals(
            TextureData p1,
            TextureData p2
    ) {
        return (p1.blockId ^ p2.blockId) |
                (p1.color ^ p2.color) |
                (p1.normal ^ p2.normal) |
                (p1.specular ^ p2.specular);
    }
}
