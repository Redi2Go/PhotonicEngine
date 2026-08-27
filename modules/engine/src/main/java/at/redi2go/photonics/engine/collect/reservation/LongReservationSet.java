package at.redi2go.photonics.engine.collect.reservation;

class LongReservationSet implements ReservationSet {
    private long contents;

    LongReservationSet(long contents) {
        this.contents = contents;
    }

    @Override
    public int reserveInt() {
        int key = Bits.firstEmptyKey(contents);
        if (key == Long.SIZE) return -1;

        contents = Bits.set(contents, key);
        return key;
    }

    @Override
    public boolean contains(int value) {
        if (value >= Long.SIZE) return false;

        return Bits.isSet(contents, value);
    }

    @Override
    public void removeInt(int value) {
        if (value >= Long.SIZE) return;
        contents = Bits.unset(contents, value);
    }
}
