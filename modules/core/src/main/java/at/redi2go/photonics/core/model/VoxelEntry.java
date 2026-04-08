package at.redi2go.photonics.core.model;

public class VoxelEntry {
    private VoxelEntry() {

    }

    public static boolean isAir(int entry) {
        return entry != Integer.MAX_VALUE && entry > 0;
    }

    public static boolean isData(int entry) {
        return entry <= 0;
    }

    public static int toAir(int x1, int y1, int z1, int x2, int y2, int z2) {
        return  (x1 << 0) |
                (y1 << 5) |
                (z1 << 10) |
                ((x2 - 1) << 15) |
                ((y2 - 1) << 20) |
                ((z2 - 1) << 25);
    }

    public static int getAirX1(int airEntry) {
        return (airEntry >> 0) & 0b11111;
    }

    public static int getAirY1(int airEntry) {
        return (airEntry >> 5) & 0b11111;
    }

    public static int getAirZ1(int airEntry) {
        return (airEntry >> 10) & 0b11111;
    }

    public static int getAirX2(int airEntry) {
        return ((airEntry >> 15) & 0b11111) + 1;
    }

    public static int getAirY2(int airEntry) {
        return ((airEntry >> 20) & 0b11111) + 1;
    }

    public static int getAirZ2(int airEntry) {
        return ((airEntry >> 25) & 0b11111) + 1;
    }
}
