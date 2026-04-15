package at.redi2go.photonics.core.util;

//TODO: Comment this
public class IntPacking {
    private static int bitsPerEntry(int maxElement) {
        int result = (32 - Integer.numberOfLeadingZeros(maxElement));
        int highestBit = Integer.highestOneBit(result);

        return result == highestBit ? result : highestBit << 1;
    }

    public static int shiftFactor(int maxElement) {
        if ((maxElement & ~1) == 0) return 5;

        return 5 - (31 - Integer.numberOfLeadingZeros(bitsPerEntry(maxElement)));
    }

    public static int dataOffset(int realIndex, int shift) {
        return realIndex >> shift;
    }

    public static int sectionLength(int shift) {
        return 1 << shift;
    }

    public static int sectionIndex(int realIndex, int shift) {
        return realIndex & (sectionLength(shift) - 1);
    }

    public static int valueShift(int shift) {
        return 5 - shift;
    }

    public static int valueMask(int shift) {
        return (-1 >>> (32 - (1 << valueShift(shift))));
    }

    public static int getValue(int sectionData, int sectionIndex, int shift) {
        return (sectionData >>> (sectionIndex << valueShift(shift))) & valueMask(shift);
    }

    public static int setValue(int sectionData, int sectionIndex, int newValue, int shift) {
        int valueShift = sectionIndex << valueShift(shift);
        return (sectionData & ~(valueMask(shift) << valueShift)) | (newValue << valueShift);
    }

    private IntPacking() {}
}
