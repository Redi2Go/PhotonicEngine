package at.redi2go.photonics.engine.collect.reservation;

public interface ReservationSet {
    /**
     * @return The next unused int, or {@code -1} if this set is full
     */
    int reserveInt();

    /**
     * @return {@code true} if {@code value} is contained in this set
     */
    boolean contains(int value);

    /**
     * Removes {@code value} from this set
     */
    void removeInt(int value);

    static void checkPositive(int value, String debugName) {
        if (value < 1)
            throw new IllegalArgumentException(debugName + " must be greater than 0 (was " + value + ")");
    }

    private static ReservationSet create(int capacity, long defaultValue) {
        checkPositive(capacity, "capacity");
        if (capacity <= 64) return new LongReservationSet(defaultValue);

        int depth = 6;
        while ((64L << depth) < capacity)
            depth += 6;

        return new TreeReservationSet(depth, capacity, defaultValue);
    }

    static ReservationSet noneSet(int capacity) {
        return create(capacity, Bits.NONE);
    }

    static ReservationSet allSet(int capacity) {
        return create(capacity, Bits.ALL);
    }
}
