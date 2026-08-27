package at.redi2go.photonics.engine.collect.reservation;

class Bits {
    static final long ALL = 0L;
    static final long NONE = -1L;

    static boolean isAllSet(long mask) {
        return mask == ALL;
    }

    static boolean isNoneSet(long mask) {
        return mask == NONE;
    }

    static boolean isSet(long value, int key) {
        return (value & (1L << key)) == 0L;
    }

    static long set(long value, int key) {
        return value & ~(1L << key);
    }

    static long unset(long value, int key) {
        return value | (1L << key);
    }

    static int firstEmptyKey(long value) {
        return Long.numberOfTrailingZeros(value);
    }

    static int toKey(int value, int depth) {
        return (value >> depth) & 63;
    }

    static int toPrefix(int value, int depth) {
        return value << depth;
    }

    static int downOne(int value) {
        return value - 6;
    }
}
