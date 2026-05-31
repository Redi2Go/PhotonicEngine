package at.redi2go.photonics.common.test;

public class BlockEntry implements TreeEntry {
    public static final BlockEntry INSTANCE = new BlockEntry();

    private BlockEntry() {

    }

    @Override
    public int depth() {
        return -1;
    }
}
