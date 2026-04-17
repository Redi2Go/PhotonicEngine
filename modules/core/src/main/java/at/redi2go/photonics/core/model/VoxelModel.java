package at.redi2go.photonics.core.model;

import org.joml.Vector3ic;

public interface VoxelModel {
    /**
     * The dimensions of the model. Each component must always be a power of 2.
     */
    Vector3ic size();

    boolean contains(int x, int y, int z);

    default boolean contains(Vector3ic pos) {
        return contains(pos.x(), pos.z(), pos.y());
    }

    int get(int x, int y, int z);

    default int get(Vector3ic pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    default int getAir(int x, int y, int z) {
        return VoxelEntry.getAir(get(x, y, z));
    }

    default int getAir(Vector3ic pos) {
        return getAir(pos.x(), pos.y(), pos.z());
    }

    default int getData(int x, int y, int z) {
        return VoxelEntry.getData(get(x, y, z));
    }

    default int getData(Vector3ic pos) {
        return getData(pos.x(), pos.y(), pos.z());
    }

    static int expandBits(int value) {
        int v = value & 0x1F;
        v = (v | (v << 16)) & 0x030000FF;
        v = (v | (v << 8)) & 0x0300F00F;
        v = (v | (v << 4)) & 0x030C30C3;
        v = (v | (v << 2)) & 0x09249249;
        return v;
    }

    static int shrinkBits(int value) {
        int v = value & 0x09249249;

        v = (v & 0x030C30C3) | ((v >> 2) & 0x030C30C3);
        v = (v & 0x0300F00F) | ((v >> 4) & 0x0300F00F);
        v = (v & 0x030000FF) | ((v >> 8) & 0x030000FF);
        v = (v & 15) | ((v >> 16) & 15);

        return v;
    }

    static int toVoxelIndex(int x, int y, int z) {
        return expandBits(x) | (expandBits(y) << 1) | (expandBits(z) << 2);
    }

    static int toVoxelIndex(int x, int y, int z, int magnitude) {
        return toVoxelIndex(
                (x >> magnitude) & 15,
                (y >> magnitude) & 15,
                (z >> magnitude) & 15
        );
    }

    static int makeAirEntry(int index) {
        int x = shrinkBits(index);
        int y = shrinkBits(index >> 1);
        int z = shrinkBits(index >> 2);

        return VoxelEntry.toAir(
                x,
                y,
                z,
                x + 1,
                y + 1,
                z + 1
        );
    }


    static boolean contains(int x, int y, int z, int width, int height, int depth) {
        // Dumb way to do this check without using branches
        // From my testing its around 20x faster than checking <c> >= 0 && <c> < size.<c>() x3
        return ((x | y | z |
                ~(x - width) |
                ~(y - height) |
                ~(z - depth)
        ) & Integer.MIN_VALUE) == 0;
    }
}
