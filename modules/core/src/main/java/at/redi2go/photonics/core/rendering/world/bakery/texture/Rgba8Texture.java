package at.redi2go.photonics.core.rendering.world.bakery.texture;

import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import org.joml.Math;

public class Rgba8Texture implements CpuTexture {
    private final int width, height;

    private final int[] color;
    public Rgba8Texture(
            int width, int height,
            int[] colorTexture
    ) {
        this.width = width;
        this.height = height;
        this.color = colorTexture;
    }

    @Override
    public TextureData sample(int blockId, float u, float v) {
        int realU = Math.clamp(Math.round(u * (width - 0.5f)), 0, width - 1);
        int realV = Math.clamp(Math.round(v * (height - 0.5f)), 0, height - 1);

        int index = (width * realV) + realU;

        return new TextureData(
                blockId,
                fromABGR(color[index])
        );
    }

    private static int fromABGR(int value) {
        return Integer.reverseBytes(Integer.rotateLeft(value, 8));
    }
}
