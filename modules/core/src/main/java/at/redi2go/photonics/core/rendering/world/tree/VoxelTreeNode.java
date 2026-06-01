package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3L;
import org.joml.Vector3i;

public abstract class VoxelTreeNode implements VoxelTreeEntry {
    public static final int SIZE_LENGTH = 4;
    public static final int ENTRIES_SIZE = SIZE_LENGTH * SIZE_LENGTH * SIZE_LENGTH;

    protected final int depth;

    protected int size = 0;
    protected final @Nullable VoxelTreeEntry[] data = new VoxelTreeEntry[ENTRIES_SIZE];

    protected VoxelTreeNode(int depth) {
        this.depth = depth;
    }

    @Override
    public int depth() {
        return depth;
    }

    public int magnitude() {
        return depth << 1;
    }

    protected abstract VoxelTreeNode createNode(Vector3i pos);

    protected VoxelTreeEntry merge(@Nullable VoxelTreeEntry oldEntry, VoxelTreeEntry newEntry) {
        if (oldEntry instanceof Disposable disposable)
            disposable.close();

        return newEntry;
    }

    protected void onChanged() {}

    public void insertEntry(Vector3i pos, @NonNls VoxelTreeEntry entry) {
        int index = indexOf(pos, magnitude());
        int targetDepth = entry.depth() + 1;
        if (targetDepth < 0) throw new IllegalStateException("depth was less than -1");

        var previous = data[index];
        if (previous == entry) return;
        if (previous == null) size++;

        if (depth == targetDepth) {
            var newEntry = merge(previous, entry);
            data[index] = newEntry;

            if (newEntry != previous)
                onChanged();

            return;
        }

        if (previous == null) {
            previous = merge(null, createNode(pos));
            data[index] = previous;

            onChanged();
        }

        ((VoxelTreeNode) previous.toMutable()).insertEntry(pos, entry);
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

    private int indexOf(Vector3i pos, int magnitude) {
        int x = ((pos.x >> magnitude) & 3);
        int y = ((pos.y >> magnitude) & 3);
        int z = ((pos.z >> magnitude) & 3);

        return x + (z << 2) + (y << 4);
    }
}
