package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
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

    private final BlockMergeMode mergeMode;

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
            BlockMergeMode mergeMode,
            int depth,
            Vector3i pos
    ) {
        super(depth);

        this.worldManager = worldManager;
        this.allocator = allocator;
        this.memory = allocator.allocateEntryList(true, 0);

        this.mergeMode = mergeMode;

        int sideLength = blockSideLength();
        this.minPos = new Vector3i(
                pos.x & -sideLength,
                pos.y & -sideLength,
                pos.z & -sideLength
        );

        this.maxPos = new Vector3i(sideLength).add(minPos);
    }

    @Override
    public int magnitude() {
        return (depth - 2) << 1;
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

    @Override
    public void insertEntry(Vector3i pos, VoxelTreeEntry entry) {
        if (entry instanceof WorldNode node) {
            containedRegions.addAll(node.containedRegions);
        } else if (entry instanceof BlockEntry blockEntry) {
            containedRegions.addAll(blockEntry.regions());
        }

        requestUpload();
        super.insertEntry(pos, entry);
    }

    private void propagateEmpty() {
        var node = this;
        while (node != null && !node.hasEmptyChild) {
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

        containedRegions.removeAll(regions);

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

    public @Nullable WorldNode pruneTree() {
        if (size > 1) {
            parent = null;
            return this;
        }

        for (var entry : data) {
            if (entry instanceof WorldNode node) {
                memory.close();
                return node.pruneTree();
            }
        }

        if (size <= 0) return null;

        parent = null;
        return this;
    }

    @Override
    protected long writeEntries(VoxelEntryListMemory memory) {
        //TODO: Fix size tracking
        int tempSize = 0;
        for (var entry : data)
            if (entry != null) tempSize++;

        size = tempSize;

        return super.writeEntries(memory);
    }

    private void requestUpload() {
        if (uploadRequested) return;

        uploadRequested = true;
        worldManager.queueUpload(depth, this::upload);
    }

    private void upload() {
        childMask = writeEntries(this.memory);
        this.memory.upload();

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
        return new WorldNode(worldManager, allocator, mergeMode, depth - 1, pos);
    }

    @Override
    protected VoxelTreeEntry merge(@Nullable VoxelTreeEntry oldEntry, VoxelTreeEntry newEntry) {
        if (newEntry instanceof WorldNode node)
            node.parent = this;

        return newEntry instanceof BlockEntry blockEntry ? mergeMode.merge((BlockEntry) oldEntry, blockEntry) : super.merge(oldEntry, newEntry);
    }

    @Override
    public void close() {
        for (var entry : data) {
            if (entry instanceof Disposable disposable)
                disposable.close();
        }

        memory.close();
    }

    public static Vector3i toTreePos(Vector3i blockPos) {
        return new Vector3i(blockPos).add(LARGE_VALUE, LARGE_VALUE, LARGE_VALUE);
    }

    public static Vector3i toBlockPos(Vector3i treePos) {
        return new Vector3i(treePos).sub(LARGE_VALUE, LARGE_VALUE, LARGE_VALUE);
    }
}
