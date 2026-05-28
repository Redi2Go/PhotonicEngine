package at.redi2go.photonics.api.mc.nbt;

public interface ICompoundTag {
    boolean ph$isEmpty();

    static boolean isEqual(ICompoundTag tag1, ICompoundTag tag2) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
