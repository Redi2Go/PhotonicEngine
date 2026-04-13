package at.redi2go.photonics.core.rendering.world.block;

public class VoxelColor {
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

    public static int from(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private VoxelColor() {

    }
}