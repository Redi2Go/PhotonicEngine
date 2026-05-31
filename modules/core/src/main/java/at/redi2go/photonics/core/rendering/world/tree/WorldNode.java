package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class WorldNode extends VoxelTreeNode implements Disposable {
    public static final int LARGE_VALUE = 100_000_000;

    private final WorldManager worldManager;
    private final WorldAllocator allocator;
    private final VoxelEntryListMemory memory;

    private final Vector3i minPos;
    private final Vector3i maxPos;


    private WorldNode parent;
    private final IntSet containedRegions = new IntOpenHashSet();

    private boolean uploadRequested = false;
    private boolean hasEmptyChild = false;

    private long childMask = 0;

    public WorldNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            int depth,
            Vector3i pos
    ) {
        super(depth);

        this.worldManager = worldManager;
        this.allocator = allocator;
        this.memory = allocator.allocateEntryList(true, 0);

        int sideLength = blockSideLength();
        this.minPos = new Vector3i(
                pos.x & -sideLength,
                pos.y & -sideLength,
                pos.z & -sideLength
        );

        this.maxPos = new Vector3i(sideLength).add(minPos);
    }

    public int blockSideLength() {
        return 4 << magnitude();
    }

    public Vector3i minTreePos() {
        return minPos;
    }

    public Vector3i maxTreePos() {
        return maxPos;
    }

    public boolean containsBlock(Vector3i treePos) {
        if (treePos.x < minPos.x || treePos.x >= maxPos.x) return false;
        if (treePos.y < minPos.y || treePos.y >= maxPos.y) return false;
        if (treePos.z < minPos.z || treePos.z >= maxPos.z) return false;

        return true;
    }

    public boolean containsAnyRegion(IntSet regions) {
        for (IntIterator it = regions.intIterator(); it.hasNext(); ) {
            if (containedRegions.contains((short) it.nextInt()))
                return true;
        }

        return false;
    }

    private void propagateEmpty() {
        if (hasEmptyChild) return;

        var node = this;
        while (node != null) {
            node.hasEmptyChild = true;
            node = node.parent;
        }
    }

    @Override
    public @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
        if (!containsAnyRegion(regions)) return this;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = data[i];
            if (entry == null) continue;

            var newEntry = entry.removeRegions(regions);
            if (newEntry == null) size--;

            if (newEntry != entry && entry instanceof Disposable disposable)
                disposable.close();

            data[i] = newEntry;
        }

        if (size <= 0)
            propagateEmpty();

        return this;
    }

    public boolean removeEmpty() {
        if (!hasEmptyChild) return false;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = data[i];
            if (!(entry instanceof WorldNode worldNode)) continue;
            if (!worldNode.removeEmpty()) continue;

            worldNode.close();
            size--;
        }

        return size <= 0;
    }

    public WorldNode pruneTree() {
        if (size > 1) return this;

        for (var entry : data) {
            if (entry instanceof WorldNode node) {
                close();
                return node.pruneTree();
            }
        }

        return this;
    }

    private void requestUpload() {
        if (uploadRequested) return;

        worldManager.queueUpload(depth, this::upload);
        uploadRequested = true;
    }

    private void upload() {
        if (size == 0) return;

        this.childMask = writeEntries(memory);
        uploadRequested = false;
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        memory.setEntryFlag(false);
        memory.setEntryData(this.memory.entryData());
        memory.setChildMask(childMask);
    }

    @Override
    protected VoxelTreeNode createNode(Vector3i pos) {
        return new WorldNode(worldManager, allocator, depth - 1, pos);
    }

    @Override
    protected VoxelTreeEntry merge(@Nullable VoxelTreeEntry oldEntry, VoxelTreeEntry newEntry) {
        if (newEntry instanceof WorldNode node) {
            node.parent = this;
            containedRegions.addAll(node.containedRegions);
        }

        return super.merge(oldEntry, newEntry);
    }

    @Override
    protected void onChanged() {
        requestUpload();
    }

    @Override
    public void close() {
        memory.close();
    }

    public static Vector3i toTreePos(Vector3i blockPos) {
        return new Vector3i(blockPos).add(LARGE_VALUE, LARGE_VALUE, LARGE_VALUE);
    }

    public static Vector3i toBlockPos(Vector3i treePos) {
        return new Vector3i(treePos).sub(LARGE_VALUE, LARGE_VALUE, LARGE_VALUE);
    }
}
