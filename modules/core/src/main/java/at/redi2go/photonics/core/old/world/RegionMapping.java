package at.redi2go.photonics.core.old.world;

import at.redi2go.photonics.core.util.IntPacking;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;

/**
 * Keeps track of which voxels belong to which region while trying to minimize memory usage as much as possible.
 * This works on the assumption that most blocks will only belong to one region, and the ones that don't likely only have a few regions.
 */
public abstract class RegionMapping {
    private static final int LOWER_SHORT_MASK = 65535;

    // Has 3 purposes:
    //
    // 1) to store the current number of unique regions
    // 2) store the region when count is 1 (avoids allocating length 1 array)
    // 3) Stores the current shift for voxelMapping
    //
    // Lower 16 bits are used for #1, while the upper 16 are used for 2 & 3.
    private int state;

    // A mapping of indexes to regions.
    //
    // Will be:
    //
    // - Null when count is 1
    // - An short array when count is <= 8
    // - A Short2ShortMap
    private Object regions;

    // Tracks which voxel maps to which region.
    private int[] voxelMapping;

    public RegionMapping() {
        this.state = 0;
        this.regions = null;
        this.voxelMapping = null;
    }

    public RegionMapping(RegionMapping other) {
        this.state = other.state;
        this.regions = other.regions;
        this.voxelMapping = other.voxelMapping;
    }

    public RegionMapping(RegionMapping other, ShortSet removedRegions) {
        this(other);
    }

    protected int regionCount() {
        return state & LOWER_SHORT_MASK;
    }

    protected void setRegion(int voxelIndex, short region) {
        int count = state & LOWER_SHORT_MASK;

        if (singleAppendRegion(count, region)) return;

        if (count == 1) {
            // Will always be index 1 since if it was index 0
            // singleSetRegion would've returned true
            singleAppend(region);
            setVoxel(voxelIndex, 1);

            return;
        }

        if (count <= 8) {
            int newIndex = arrayAppend(region);
            if (newIndex != -1) {
                setVoxel(voxelIndex, newIndex);
                return;
            }
        }

        throw new NotImplementedException("TODO");
    }

    protected short getRegion(int voxelIndex) {
        int count = state & LOWER_SHORT_MASK;
        if (count < 2) return singleGetRegion();

        int index = getVoxel(voxelIndex);
        if (count <= 8) return arrayGetRegion(index);

        throw new NotImplementedException("TODO");
    }

    // Voxel array handling

    private int setVoxelShift(int capacity) {
        int shift = IntPacking.shiftFactor(capacity - 1);
        state = state & LOWER_SHORT_MASK | shift << 16;

        return shift;
    }

    private int getVoxelShift() {
        return (state >>> 16) & LOWER_SHORT_MASK;
    }

    private void initVoxelArray(int capacity) {
        int shift = setVoxelShift(capacity);

        voxelMapping = new int[RtVoxel.ENTRIES_SIZE >> shift];
    }

    private void growVoxelArray(int newCapacity) {
        int oldShift = getVoxelShift();
        int newShift = setVoxelShift(newCapacity);
        if (oldShift == newShift) return;

        int[] newData = new int[RtVoxel.ENTRIES_SIZE >> newShift];

        int oldSectionLength = IntPacking.sectionLength(oldShift);
        int newSectionLength = IntPacking.sectionLength(newShift);

        //Assumes a doubling of size
        for (int i = 0; i < newData.length; i++) {
            int offset = (i & 1) == 0 ? 0 : oldSectionLength;
            int value = 0;

            // TODO: Do this smarter with rolling shifting
            for (int s = 0; s < newSectionLength; s++)
                value = IntPacking.setValue(
                        value,
                        s,
                        IntPacking.getValue(voxelMapping[i >> 1], s + offset, oldShift),
                        newShift
                );

            newData[i] = value;
        }

        voxelMapping = newData;
    }

    private void setVoxel(int voxelIndex, int regionIndex) {
        int shift = getVoxelShift();
        int offset = IntPacking.dataOffset(voxelIndex, shift);
        int sectionIndex = IntPacking.sectionIndex(voxelIndex, shift);

        voxelMapping[offset] = IntPacking.setValue(voxelMapping[offset], sectionIndex, regionIndex, shift);
    }

    private int getVoxel(int voxelIndex) {
        int shift = getVoxelShift();
        int offset = IntPacking.dataOffset(voxelIndex, shift);
        int sectionIndex = IntPacking.sectionIndex(voxelIndex, shift);

        return IntPacking.getValue(voxelMapping[offset], sectionIndex, shift);
    }

    // Single region handling

    private boolean singleAppendRegion(int count, short region) {
        if (count == 0) {
            state = ((int) region << 16) | 1;

            return true;
        }

        return count == 1 && (state >>> 16) == ((int) region & LOWER_SHORT_MASK);
    }

    protected short singleGetRegion() {
        return (short) (state >>> 16);
    }

    private void singleAppend(short region) {
        regions = new short[]{singleGetRegion(), region};
        state = 2;

        initVoxelArray(2);
    }


    // short array handling

    private int arrayAppend(short region) {
        int count = state & LOWER_SHORT_MASK;
        short[] arr = (short[]) regions;

        if (count >= arr.length) {
            if (count == 8) return -1;

            int newSize = count << 1;
            growVoxelArray(newSize);

            arr = Arrays.copyOf(arr, newSize);
            regions = arr;
        }

        for (int i = 0; i < count; i++) {
            if (arr[i] == region)
                return i;
        }

        arr[count] = region;
        state = (state & ~LOWER_SHORT_MASK) | count + 1;

        return count;
    }

    private short arrayGetRegion(int index) {
        short[] arr = (short[]) regions;
        return arr[index];
    }
}
