package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.allocator.WorldVoxelMemory;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Queue;

public class WorldVoxel extends AbstractVoxelModel implements VoxelEntry, RtVoxel {
    private static final ThreadLocal<int[]> UPLOAD_ARRAYS = ThreadLocal.withInitial(() -> new int[RtVoxel.ENTRIES_SIZE]);

    public static final int BLOCK_DEPTH = 0;
    public static final int CHUNK_DEPTH = 1;
    public static final int CHUNK_CONTAINER_DEPTH = 2;

    private final int depth;

    protected final BlockMergeMode mergeMode;
    protected final ChunkManager chunkManager;
    protected final WorldRegistry worldRegistry;
    protected final Queue<WorldVoxel> uploadQueue;

    private final @Nullable VoxelEntry[] voxelData = new VoxelEntry[RtVoxel.ENTRIES_SIZE];
    private int voxelCount = 0;

    protected final IntSet containedRegions = new IntOpenHashSet();

    private WorldVoxelMemory memory;
    private boolean firstUpload = true;
    private boolean updateRequested = false;

    protected WorldVoxel(
            int depth,
            BlockMergeMode mergeMode,
            ChunkManager chunkManager,
            WorldRegistry worldRegistry,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(SIZE_3);

        this.depth = depth;
        this.mergeMode = mergeMode;
        this.chunkManager = chunkManager;
        this.worldRegistry = worldRegistry;
        this.uploadQueue = uploadQueue;

        this.memory = worldRegistry.worldAllocator().allocateWorldVoxel();
    }

    public int magnitude() {
        return depth << 2;
    }

    public boolean containsAnyRegion(IntSet regions) {
        for (IntIterator it = regions.intIterator(); it.hasNext(); ) {
            if (containedRegions.contains((short) it.nextInt()))
                return true;
        }

        return false;
    }

    public boolean containsChunk(int x, int y, int z) {
        return containsVoxel(x, y, z) && containsVoxel(x + 15, y + 15, z + 15);
    }

    public boolean containsChunk(Vector3i chunkBlockPos) {
        return containsChunk(chunkBlockPos.x, chunkBlockPos.y, chunkBlockPos.z);
    }


    @Override
    public int entryData() {
        return memory.entryData();
    }


    // Access methods

    private void requestUpload() {
        if (updateRequested) return;

        uploadQueue.offer(this);
        updateRequested = true;
    }

    protected VoxelEntry newMutableEntry(int depth) {
        return create(depth, mergeMode, chunkManager, worldRegistry, uploadQueue);
    }

    private void setEntry(
            int index,
            @Nullable VoxelEntry oldEntry,
            @Nullable VoxelEntry newEntry,
            boolean closeOldEntries
    ) {
        if (oldEntry == newEntry) return;

        if (oldEntry != null) {
            if (closeOldEntries)
                oldEntry.close();

            if (newEntry == null)
                voxelCount--;
        } else voxelCount++;

        requestUpload();
        voxelData[index] = newEntry;
    }

    protected void setEntry(
            int index,
            @Nullable VoxelEntry oldEntry,
            @Nullable VoxelEntry newEntry
    ) {
        setEntry(index, oldEntry, newEntry, true);
    }

    protected void setEntryUnsafe(
            int index,
            @Nullable VoxelEntry oldEntry,
            @Nullable VoxelEntry newEntry
    ) {
        setEntry(index, oldEntry, newEntry, false);
    }

    protected void setEntry(int index, @Nullable VoxelEntry newEntry) {
        setEntry(index, voxelData[index], newEntry);
    }

    protected @Nullable VoxelEntry getEntry(int index) {
        return voxelData[index];
    }

    protected @NonNls VoxelEntry getMutableEntry(int index) {
        final var entry = getEntry(index);
        if (entry != null) return entry;

        var newEntry = newMutableEntry(depth - 1);
        setEntry(index, null, newEntry);

        return newEntry;
    }


    protected boolean clearEmptyEntry(int index) {
        final var entry = getEntry(index);
        if (entry == null) return false;

        if (!(entry instanceof WorldVoxel worldVoxel)) return false;
        if (worldVoxel.voxelCount > 0) return false;

        setEntry(index, entry, null);

        return false;
    }


    // Block management methods

    @Override
    public void insertBlock(int x, int y, int z, int region, BlockEntry block) {
        int index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        containedRegions.add(region);

        getMutableEntry(index).insertBlock(
                x, y, z,
                region,
                block
        );
    }


    @Override
    public @Nullable VoxelEntry removeRegions(IntSet regions) {
        if (!containsAnyRegion(regions)) return this;

        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            final var entry = getEntry(i);
            if (entry == null) continue;

            setEntry(i, entry, entry.removeRegions(regions));
        }

        return this;
    }


    // Chunk management methods

    public void pruneEmptyVoxels() {
        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            if (!clearEmptyEntry(i)) continue;

            final var entry = getEntry(i);
            if (!(entry instanceof WorldVoxel worldVoxel)) continue;

            worldVoxel.pruneEmptyVoxels();
        }
    }

    public void insertChunk(int x, int y, int z, ChunkVoxel chunk) {
        int index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = getMutableEntry(index);

        ((WorldVoxel) entry).insertChunk(x, y, z, chunk);
        containedRegions.addAll(chunk.containedRegions);
    }

    public void removeChunkUnsafe(int x, int y, int z) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = getMutableEntry(index);

        ((WorldVoxel) entry).removeChunkUnsafe(x, y, z);
        containedRegions.clear();
    }

    @Override
    public @Nullable VoxelEntry build() {
        return 0 >= voxelCount ? null : this;
    }

    public void upload() {
        var memory = this.memory;
        if (memory == null) return;

        int optimized = firstUpload ? 1 : 0;

        int[] data = UPLOAD_ARRAYS.get();

        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            final var oldEntry = getEntry(i);
            var newEntry = oldEntry != null ? oldEntry.build() : null;

            setEntry(i, oldEntry, newEntry);

            var oldEntryData = memory.getEntry(i);
            var newEntryData = newEntry == null ? VoxelModel.makeAirEntry(i) : VoxelEntry.toData(newEntry.entryData());

            int diff = VoxelEntry.entryDiff(oldEntryData, newEntryData);

            optimized |= diff;
            data[i] = !firstUpload && (diff == 0 && newEntry == null) ? oldEntryData : newEntryData;
        }

        if (voxelCount != RtVoxel.ENTRIES_SIZE && optimized != 0)
            new OptimizeWrapper(data).optimize();

        memory.setData(data);
        memory.upload();

        updateRequested = false;
        firstUpload = false;
    }

    @Override
    public void close() {
        memory.close();
        memory = null;

        if (voxelCount == 0) return;

        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++)
            setEntry(i, null);
    }

    @Override
    public int blockSideLength() {
        return 1 << magnitude();
    }

    @Override
    public int voxelSideLength() {
        return SIDE_LENGTH << magnitude();
    }

    @Override
    protected int get(int index) {
        throw new UnsupportedOperationException("get");
    }

    @Override
    protected void set(int index, int value) {
        throw new UnsupportedOperationException("set");
    }

    public static WorldVoxel create(
            int depth,
            BlockMergeMode mergeMode,
            ChunkManager chunkManager,
            WorldRegistry worldRegistry,
            Queue<WorldVoxel> uploadQueue
    ) {
        return switch (depth) {
            case BLOCK_DEPTH -> throw new IllegalArgumentException("Tried to create world voxel for block depth (0)");
            case CHUNK_DEPTH -> new ChunkVoxel(CHUNK_DEPTH, mergeMode, chunkManager, worldRegistry, uploadQueue);
            case CHUNK_CONTAINER_DEPTH -> new ChunkContainerVoxel(CHUNK_CONTAINER_DEPTH, mergeMode, chunkManager, worldRegistry, uploadQueue);

            default -> new WorldVoxel(depth, mergeMode, chunkManager, worldRegistry, uploadQueue);
        };
    }


    private static class OptimizeWrapper extends AbstractVoxelModel {
        private final int[] data;

        public OptimizeWrapper(int[] data) {
            super(RtVoxel.SIZE_3);

            this.data = data;
        }

        @Override
        protected int get(int index) {
            return data[index];
        }

        @Override
        protected void set(int index, int value) {
            data[index] = value;
        }

        @Override
        public void optimize() {
            super.optimize();
        }
    }
}
