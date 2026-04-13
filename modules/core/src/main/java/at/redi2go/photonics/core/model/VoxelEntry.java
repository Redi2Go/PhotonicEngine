package at.redi2go.photonics.core.model;

public class VoxelEntry {
    public static boolean isAir(int entry) {
        return (entry & Integer.MAX_VALUE) == 0;
    }

    public static int toAir(int value) {
        if ((value & Integer.MIN_VALUE) != 0)
            throw new IllegalArgumentException("Air must be positive");

        return value;
    }

    public static int getAir(int entry) {
        if ((entry & Integer.MIN_VALUE) != 0)
            throw new IllegalArgumentException("Entry was not an air entry");

        return entry;
    }

    public static boolean isData(int entry) {
        return (entry & Integer.MIN_VALUE) != 0;
    }

    public static int getData(int entry) {
        if ((entry & Integer.MIN_VALUE) == 0)
            throw new IllegalArgumentException("Entry was not a data entry");

        return entry & Integer.MAX_VALUE;
    }

    public static int toData(int value) {
        if ((value & Integer.MIN_VALUE) != 0)
            throw new IllegalArgumentException("Data must be positive");

        return value | Integer.MIN_VALUE;
    }

    public static int toAir(int x1, int y1, int z1, int x2, int y2, int z2) {
        return  (x1 << 0)        |
                (y1 << 5)        |
                (z1 << 10)       |
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

    private VoxelEntry() {

    }
}
