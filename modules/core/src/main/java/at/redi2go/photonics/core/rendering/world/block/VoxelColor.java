package at.redi2go.photonics.core.rendering.world.block;

import org.joml.Vector4f;

public class VoxelColor {
    private static final float INV_255 = 1f / 255f;

    public static final int WHITE = -1;

    public static int r(int packedColor) {
        return (packedColor >>> 16) & 0xff;
    }

    public static int g(int packedColor) {
        return (packedColor >>> 8) & 0xff;
    }

    public static int b(int packedColor) {
        return packedColor & 0xff;
    }

    public static int a(int packedColor) {
        return (packedColor >>> 24) & 0xff;
    }

    public static boolean gt(int packedColor1, int packedColor2) {
        return a(packedColor1) > a(packedColor2) || (packedColor1 & 16777215) > (packedColor2 & 16777215);
    }

    public static Vector4f toVector(int packedColor, Vector4f result) {
        result.x = r(packedColor) * INV_255;
        result.y = g(packedColor) * INV_255;
        result.z = b(packedColor) * INV_255;
        result.w = a(packedColor) * INV_255;

        return result;
    }

    public static Vector4f toVector(int packedColor) {
        return toVector(packedColor, new Vector4f());
    }

    public static int from(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int fromVector(Vector4f color) {
        return from(
                (int) (color.x * 255f),
                (int) (color.y * 255f),
                (int) (color.z * 255f),
                (int) (color.w * 255f)
        );
    }

    public static int applyTint(int color, int tint) {
        if (tint == WHITE) return color;

        return from(
                ((r(color) + 1) * r(tint)) >> 8,
                ((g(color) + 1) * g(tint)) >> 8,
                ((b(color) + 1) * b(tint)) >> 8,
                ((a(color) + 1) * a(tint)) >> 8
        );
    }

    private VoxelColor() {

    }
}