package at.redi2go.photonics.core.model;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public interface VoxelEntry extends Disposable {
    int entryData();

    void insertVoxel(
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    );

    void insertBlock(
            int x, int y, int z,
            short region,
            BlockEntry block
    );

    @Nullable VoxelEntry removeRegions(ShortSet regions);

    VoxelEntry toMutableEntry();

    @Nullable VoxelEntry build();

    static boolean isAir(int entry) {
        return (entry & Integer.MAX_VALUE) == 0;
    }

    static int toAir(int value) {
        if ((value & Integer.MIN_VALUE) != 0)
            throw new IllegalArgumentException("Air must be positive");

        return value;
    }

    static int getAir(int entry) {
        if ((entry & Integer.MIN_VALUE) != 0)
            throw new IllegalArgumentException("Entry was not an air entry");

        return entry;
    }

    static boolean isData(int entry) {
        return (entry & Integer.MIN_VALUE) != 0;
    }

    static int getData(int entry) {
        if ((entry & Integer.MIN_VALUE) == 0)
            throw new IllegalArgumentException("Entry was not a data entry");

        return entry & Integer.MAX_VALUE;
    }

    static int toData(int value) {
        if ((value & Integer.MIN_VALUE) != 0)
            throw new IllegalArgumentException("Data must be positive");

        return value | Integer.MIN_VALUE;
    }

    static int toAir(int x1, int y1, int z1, int x2, int y2, int z2) {
        return (x1 << 0) |
                (y1 << 5) |
                (z1 << 10) |
                ((x2 - 1) << 15) |
                ((y2 - 1) << 20) |
                ((z2 - 1) << 25);
    }

    static int getAirX1(int airEntry) {
        return (airEntry >> 0) & 0b11111;
    }

    static int getAirY1(int airEntry) {
        return (airEntry >> 5) & 0b11111;
    }

    static int getAirZ1(int airEntry) {
        return (airEntry >> 10) & 0b11111;
    }

    static int getAirX2(int airEntry) {
        return ((airEntry >> 15) & 0b11111) + 1;
    }

    static int getAirY2(int airEntry) {
        return ((airEntry >> 20) & 0b11111) + 1;
    }

    static int getAirZ2(int airEntry) {
        return ((airEntry >> 25) & 0b11111) + 1;
    }
}
