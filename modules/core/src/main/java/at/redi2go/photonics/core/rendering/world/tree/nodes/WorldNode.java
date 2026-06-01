package at.redi2go.photonics.core.rendering.world.tree.nodes;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.rendering.world.WorldManager;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryListMemory;
import at.redi2go.photonics.core.rendering.world.allocator.VoxelEntryMemory;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.tree.BlockMergeMode;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeEntry;
import at.redi2go.photonics.core.rendering.world.tree.VoxelTreeNode;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.concurrent.atomic.AtomicBoolean;

public class WorldNode extends VoxelTreeNode implements Disposable {
    private static final int CLOSED = 2;

    protected final WorldManager worldManager;
    protected final WorldAllocator allocator;
    protected final BlockMergeMode mergeMode;

    private boolean isClosed = false;
    private final VoxelEntryListMemory memory;

    private final AtomicBoolean uploadRequested = new AtomicBoolean(false);
    private boolean hasEmptyChild = false;

    private long childMask = 0;

    private final IntSet containedRegions = new IntOpenHashSet();

    protected WorldNode parent;

    private final Vector3i minBounds;
    private final Vector3i maxBounds;

    WorldNode(
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            int depth,
            Vector3i pos
    ) {
        super(depth);

        this.worldManager = worldManager;
        this.allocator = allocator;
        this.mergeMode = mergeMode;
        this.memory = allocate(allocator);

        int sideLength = blockSideLength();
        this.minBounds = new Vector3i(
                pos.x & -sideLength,
                pos.y & -sideLength,
                pos.z & -sideLength
        );

        this.maxBounds = new Vector3i(sideLength).add(minBounds);
    }

    protected VoxelEntryListMemory allocate(WorldAllocator allocator) {
        return allocator.allocateEntryList(true, 0);
    }

    @Override
    public int magnitude() {
        return (depth() - BLOCK_CONTAINER_DEPTH) << 1;
    }

    public int blockSideLength() {
        return 4 << magnitude();
    }

    public Vector3i minBounds() {
        return minBounds;
    }

    public Vector3i maxBounds() {
        return maxBounds;
    }

    public boolean isInBounds(Vector3i pos) {
        if (pos.x < minBounds.x || pos.x >= maxBounds.x) return false;
        if (pos.y < minBounds.y || pos.y >= maxBounds.y) return false;
        if (pos.z < minBounds.z || pos.z >= maxBounds.z) return false;

        return true;
    }

    public boolean containsAnyRegion(IntSet regions) {
        for (IntIterator it = regions.intIterator(); it.hasNext(); ) {
            if (containedRegions.contains(it.nextInt()))
                return true;
        }

        return false;
    }

    public void recenter(Vector3i offset) {
        if (parent != null)
            throw new IllegalStateException("Cannot recenter while in tree");

        minBounds.add(offset);
        maxBounds.add(offset);
    }

    @Override
    protected void onChanged() {
        requestUpload();

        if (isEmpty()) {
            var node = this;
            while (node != null && !node.hasEmptyChild) {
                node.hasEmptyChild = true;
                node = node.parent;
            }
        }
    }

    protected void insertRegions(VoxelTreeEntry entry) {
        if (entry instanceof WorldNode worldNode)
            containedRegions.addAll(worldNode.containedRegions);
        else if (entry instanceof BlockEntry blockEntry)
            containedRegions.addAll(blockEntry.regions());

    }

    @Override
    public void insertEntry(int x, int y, int z, @NonNls VoxelTreeEntry entry) {
        insertRegions(entry);

        int index = indexOf(x, y, z, magnitude());
        int targetDepth = entry.depth() + 1;
        if (targetDepth < 0) throw new IllegalStateException("depth was less than -1");

        if (depth() == targetDepth) {
            setEntry(index, entry);
            if (entry instanceof WorldNode node)
                node.parent = this;

            return;
        }

        var previous = getEntry(index);
        if (previous == null) {
            previous = createNode(x, y, z);
            setEntry(index, previous);
        }

        ((VoxelTreeNode) previous.toMutable()).insertEntry(x, y, z, entry);
    }

    @Override
    public @Nullable VoxelTreeEntry removeRegions(IntSet regions) {
        if (!containsAnyRegion(regions)) return this;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = getEntry(i);
            if (entry == null) continue;

            var ignored = replaceEntry(i, entry.removeRegions(regions));
        }

        containedRegions.removeAll(regions);
        return this;
    }

    public @Nullable WorldNode removeEmpty() {
        if (!hasEmptyChild) return this;

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = getEntry(i);
            if (!(entry instanceof WorldNode worldNode)) continue;

            var ignored = replaceEntry(i, worldNode.removeEmpty());
        }

        if (!isEmpty()) return this;

        close();
        return null;
    }

    public @Nullable WorldNode trim() {
        if (size() > 1) {
            this.parent = null;
            return this;
        }

        for (int i = 0; i < ENTRIES_SIZE; i++) {
            var entry = getEntry(i);
            if (!(entry instanceof WorldNode worldNode)) continue;

            var ignored = replaceEntry(i, null);
            close();

            return worldNode.trim();
        }

        if (isEmpty()) {
            close();
            return null;
        } else {
            parent = null;
            return this;
        }
    }

    @Override
    public void close() {
        for (int i = 0; i < ENTRIES_SIZE; i++) {
            if (getEntry(i) instanceof Disposable disposable)
                disposable.close();
        }

        memory.close();
        isClosed = true;
    }


    // Uploading

    private void requestUpload() {
//        if (uploadRequested.compareAndSet(false, true))
//            worldManager.queueUpload(depth(), this::upload);
//
//        if (parent != null)
//            parent.requestUpload();
    }

    private void upload() {
        if (isClosed) return;

        childMask = writeEntries(this.memory);
        this.memory.upload();

        uploadRequested.set(false);
    }

    @Override
    public void uploadTo(VoxelEntryMemory memory) {
        childMask = writeEntries(this.memory);
        this.memory.upload();

        memory.setEntryFlag(depth() == BLOCK_CONTAINER_DEPTH);
        memory.setEntryData(this.memory.entryData());
        memory.setChildMask(childMask);
    }


    // Misc

    @Override
    protected VoxelTreeNode createNode(int x, int y, int z) {
        return create(x, y, z, worldManager, allocator, mergeMode, depth() - 1);
    }

    public static WorldNode create(
            int x, int y, int z, 
            WorldManager worldManager,
            WorldAllocator allocator,
            BlockMergeMode mergeMode,
            int depth
    ) {
        var pos = new Vector3i(x, y, z);

        return switch (depth) {
            case BLOCK_CONTAINER_DEPTH -> new BlockContainerNode(worldManager, allocator, mergeMode, pos);

            case CHUNK_DEPTH -> new ChunkNode(worldManager, allocator, mergeMode, pos);

            case CHUNK_CONTAINER_DEPTH -> new ChunkContainerNode(worldManager, allocator, mergeMode, pos);

            default -> new WorldNode(worldManager, allocator, mergeMode, depth, pos);
        };
    }
}
