package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public abstract class VoxelTreeNode implements VoxelTreeEntry {
    public static final int SIZE_LENGTH = 4;
    public static final int ENTRIES_SIZE = SIZE_LENGTH * SIZE_LENGTH * SIZE_LENGTH;

    private final int depth;

    private int size = 0;
    private final @Nullable VoxelTreeEntry[] data = new VoxelTreeEntry[ENTRIES_SIZE];

    protected VoxelTreeNode(int depth) {
        this.depth = depth;
    }

    protected VoxelTreeNode(
            int depth,
            int size,
            VoxelTreeEntry[] entries
    ) {
        this(depth);

        this.size = size;
        System.arraycopy(entries, 0, data, 0, ENTRIES_SIZE);
    }

    @Override
    public int depth() {
        return depth;
    }

    protected int size() {
        return size;
    }

    protected boolean isEmpty() {
        return size == 0;
    }

    public int magnitude() {
        return depth << 1;
    }

    protected abstract VoxelTreeNode createNode(int x, int y, int z);

    protected @Nullable VoxelTreeEntry getEntry(int index) {
        return data[index];
    }

    protected @Nullable VoxelTreeEntry replaceEntry(int index, @Nullable VoxelTreeEntry entry) {
        var previous = data[index];
        if (previous == entry) return null;

        onChanged();

        data[index] = entry;

        if (previous == null)
            size++;
        else if (entry == null)
            size--;

        return previous;
    }

    protected void setEntry(int index, @Nullable VoxelTreeEntry entry) {
        var result = replaceEntry(index, entry);
        if (result instanceof Disposable disposable)
            disposable.close();
    }

    public void insertEntry(int x, int y, int z, @NonNls VoxelTreeEntry entry) {
        int index = indexOf(x, y, z, magnitude());
        int targetDepth = entry.depth() + 1;
        if (targetDepth < 0) throw new IllegalStateException("depth was less than -1");

        if (depth == targetDepth) {
            setEntry(index, entry);
            return;
        }

        var previous = getEntry(index);
        if (previous == null) {
            previous = createNode(x, y, z);
            setEntry(index, previous);
        }

        ((VoxelTreeNode) previous.toMutable()).insertEntry(x, y, z, entry);
    }

    public void insertEntry(Vector3i pos, @NonNls VoxelTreeEntry entry) {
        insertEntry(pos.x, pos.y, pos.z, entry);
    }

    protected long writeEntries(VoxelEntryListMemory memory) {
        memory.resize(size);

        long mask = 0;
        int i = 0;

        for (var entry : data) {
            mask >>>= 1;
            if (entry == null) continue;

            mask |= Long.MIN_VALUE;
            entry.uploadTo(memory.get(i++));
        }

        return mask;
    }

    protected void onChanged() { }


    protected static int indexOf(int x, int y, int z, int magnitude) {
        x = ((x >> magnitude) & 3);
        y = ((y >> magnitude) & 3);
        z = ((z >> magnitude) & 3);

        return x + (z << 2) + (y << 4);
    }
}
