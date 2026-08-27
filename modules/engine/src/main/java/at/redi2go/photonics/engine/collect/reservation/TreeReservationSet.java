package at.redi2go.photonics.engine.collect.reservation;

import java.util.Arrays;

class TreeReservationSet implements ReservationSet {
    private final int depth;
    private final int capacity;

    private BitContainer container;

    TreeReservationSet(int depth, int capacity, long defaultValue) {
        this.depth = depth;
        this.capacity = capacity;

        this.container = getDefaultBitContainer(defaultValue);
    }

    @Override
    public int reserveInt() {
        BitContainer container = this.container;
        if (container == BitContainer.NONE_SET) {
            container = createBitContainer(depth, Bits.NONE);
            this.container = container;
        }

        int index = container.reserveInt(depth);
        if (container.isFull()) this.container = BitContainer.ALL_SET;

        return index < capacity ? index : -1;
    }

    @Override
    public boolean contains(int value) {
        return value >= 0 && value < capacity && container.contains(value, depth);
    }

    @Override
    public void removeInt(int value) {
        if (value < 0 || value >= capacity) return;

        BitContainer container = this.container;
        if (container == BitContainer.ALL_SET) {
            container = createBitContainer(depth, Bits.ALL);
            this.container = container;
        }

        container.removeInt(value, depth);
        if (container.isEmpty()) this.container = BitContainer.NONE_SET;
    }

    private static abstract class BitContainer {
        long isFullMask;
        long isEmptyMask;

        BitContainer(long isFullMask, long isEmptyMask) {
            this.isFullMask = isFullMask;
            this.isEmptyMask = isEmptyMask;
        }

        BitContainer() {
            this(0, 0);
        }

        boolean isFull() {
            return Bits.isAllSet(isFullMask);
        }

        boolean isEmpty() {
            return Bits.isAllSet(isEmptyMask);
        }

        abstract int reserveInt(int depth);

        abstract boolean contains(int value, int depth);

        abstract void removeInt(int value, int depth);

        static final BitContainer ALL_SET = new BitContainer(Bits.ALL, Bits.NONE) {
            @Override
            int reserveInt(int depth) {
                return -1;
            }

            @Override
            boolean contains(int value, int depth) {
                return true;
            }

            @Override
            void removeInt(int value, int depth) {
                throw new UnsupportedOperationException("removeInt");
            }
        };

        static final BitContainer NONE_SET = new BitContainer(Bits.NONE, Bits.ALL) {
            @Override
            int reserveInt(int depth) {
                throw new UnsupportedOperationException("reserveInt");
            }

            @Override
            boolean contains(int value, int depth) {
                return false;
            }

            @Override
            void removeInt(int value, int depth) {

            }
        };
    }

    private static class RootContainer extends BitContainer {
        private final long[] children = new long[Long.SIZE];

        RootContainer(long defaultValue) {
            Arrays.fill(children, defaultValue);

            isFullMask = defaultValue == Bits.ALL ? Bits.ALL : Bits.NONE;
            isEmptyMask = defaultValue == Bits.NONE ? Bits.ALL : Bits.NONE;
        }

        @Override
        int reserveInt(int depth) {
            int childKey = Bits.firstEmptyKey(isFullMask);
            if (childKey == Long.SIZE) return -1;

            long childData = children[childKey];

            int key = Bits.firstEmptyKey(childData);
            childData = Bits.set(childData, key);
            children[childKey] = childData;

            if (Bits.isAllSet(childData)) isFullMask = Bits.set(isFullMask, childKey);
            if (Bits.isSet(isEmptyMask, childKey)) isEmptyMask = Bits.unset(isEmptyMask, childKey);

            return Bits.toPrefix(childKey, 6) | Bits.toPrefix(key, 0);
        }

        @Override
        boolean contains(int value, int depth) {
            return Bits.isSet(children[Bits.toKey(value, 6)], Bits.toKey(value, 0));
        }

        @Override
        void removeInt(int value, int depth) {
            int childKey = Bits.toKey(value, 6);
            if (Bits.isSet(isEmptyMask, childKey)) return;

            int key = Bits.toKey(value, 0);

            long childData = Bits.unset(children[childKey], key);
            children[childKey] = childData;

            if (Bits.isSet(isFullMask, childKey)) isFullMask = Bits.unset(isFullMask, childKey);
            if (Bits.isNoneSet(childData)) isEmptyMask = Bits.set(isEmptyMask, childKey);
        }
    }

    private static class InceptionContainer extends BitContainer {
        private final BitContainer[] children = new BitContainer[Long.SIZE];

        InceptionContainer(long defaultValue) {
            Arrays.fill(children, getDefaultBitContainer(defaultValue));

            isFullMask = defaultValue == Bits.ALL ? Bits.ALL : Bits.NONE;
            isEmptyMask = defaultValue == Bits.NONE ? Bits.ALL : Bits.NONE;
        }

        @Override
        int reserveInt(int depth) {
            int childKey = Bits.firstEmptyKey(isFullMask);
            if (childKey == Long.SIZE) return -1;

            BitContainer childData;
            if (Bits.isSet(isEmptyMask, childKey)) {
                isFullMask = Bits.unset(isEmptyMask, childKey);

                childData = createBitContainer(depth, Bits.NONE);
                children[childKey] = childData;
            } else childData = children[childKey];

            int suffix = childData.reserveInt(Bits.downOne(depth));
            int index = Bits.toPrefix(childKey, depth) | suffix;

            if (childData.isFull()) {
                isFullMask = Bits.set(isFullMask, childKey);
                children[childKey] = BitContainer.ALL_SET;
            }

            return  index;
        }

        @Override
        boolean contains(int value, int depth) {
            return children[Bits.toKey(value, depth)].contains(value, Bits.downOne(depth));
        }

        @Override
        void removeInt(int value, int depth) {
            int childKey = Bits.toKey(value, depth);
            if (Bits.isSet(isEmptyMask, childKey)) return;

            BitContainer childData;
            if (Bits.isSet(isFullMask, childKey)) {
                childData = createBitContainer(depth, Bits.ALL);
                children[childKey] = childData;
            } else childData = children[childKey];

            childData.removeInt(value, Bits.downOne(depth));

            if (childData.isEmpty()) {
                isEmptyMask = Bits.set(isEmptyMask, childKey);
                children[childKey] = BitContainer.NONE_SET;
            }
        }
    }

    private static BitContainer getDefaultBitContainer(long defaultValue) {
        return defaultValue == Bits.ALL ? BitContainer.ALL_SET : BitContainer.NONE_SET;
    }

    private static BitContainer createBitContainer(int depth, long defaultValue) {
        return depth > 6 ? new InceptionContainer(defaultValue) : new RootContainer(defaultValue);
    }
}
