package at.redi2go.photonics.core.rendering.world.tree;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.bakery.BlockConsumer;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.Queue;

public class WorldVoxel extends AbstractVoxelModel implements VoxelEntry, RtVoxel, BlockConsumer, VoxelConsumer {
    private static final ThreadLocal<int[]> UPLOAD_ARRAYS = ThreadLocal.withInitial(() -> new int[RtVoxel.ENTRIES_SIZE]);

    public static final int BLOCK_DEPTH = 0;
    public static final int CHUNK_DEPTH = 1;
    public static final int CHUNK_CONTAINER_DEPTH = 2;

    private final int depth;

    protected final WorldCompiler compiler;
    protected final BlockRegistry blockRegistry;
    protected final IGpuBufferHeap heap;
    protected final Queue<WorldVoxel> uploadQueue;

    private final @Nullable VoxelEntry[] voxelData = new VoxelEntry[RtVoxel.ENTRIES_SIZE];
    private int voxelCount = 0;

    protected final ShortSet containedRegions = new ShortOpenHashSet();

    private MemoryView memory;
    private boolean firstUpload = true;
    private boolean updateRequested = false;

    protected WorldVoxel(
            int depth,
            WorldCompiler compiler,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(SIZE_3);

        this.depth = depth;
        this.compiler = compiler;
        this.blockRegistry = blockRegistry;
        this.heap = heap;
        this.uploadQueue = uploadQueue;

        this.memory = heap.allocateOrThrow(ENTRIES_SIZE << 2);
    }

    public int magnitude() {
        return depth << 2;
    }

    public boolean containsAnyRegion(ShortSet regions) {
        for (IntIterator it = regions.intIterator(); it.hasNext(); ) {
            if (containedRegions.contains((short) it.nextInt()))
                return true;
        }

        return false;
    }

    public boolean containsChunk(int x, int y, int z) {
        return containsVoxel(x, y, z) && containsVoxel(x + 15, y + 15, z + 15);
    }

    public boolean containsChunk(Vector3i pos) {
        return containsChunk(pos.x, pos.y, pos.z);
    }


    @Override
    public int entryData() {
        return MemoryView.intBufferBegin(memory);
    }


    // Access methods

    private void requestUpload() {
        if (updateRequested) return;

        uploadQueue.offer(this);
        updateRequested = true;
    }

    protected VoxelEntry newMutableEntry(int depth) {
        return create(depth, compiler, blockRegistry, heap, uploadQueue);
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
        final var oldEntry = getEntry(index);
        var newEntry = oldEntry == null ? newMutableEntry(depth - 1) : oldEntry.toMutableEntry();

        setEntry(index, oldEntry, newEntry);

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


    // VoxelEntry methods

    @Override
    public void insertVoxel(
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    ) {
        int index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        containedRegions.add(region);

        getMutableEntry(index).insertVoxel(
                x, y, z,
                region,
                normal,
                tint,
                textureData
        );
    }


    @Override
    public void insertBlock(int x, int y, int z, short region, BlockEntry block) {
        int index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        containedRegions.add(region);

        getMutableEntry(index).insertBlock(
                x, y, z,
                region,
                block
        );
    }

    @Override
    public @Nullable VoxelEntry removeRegions(ShortSet regions) {
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

    public void removeChunk(int x, int y, int z, short region) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = getMutableEntry(index);

        ((WorldVoxel) entry).removeChunk(x, y, z, region);
        containedRegions.remove(region);
    }

    public void removeChunkUnsafe(int x, int y, int z) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = getMutableEntry(index);

        ((WorldVoxel) entry).removeChunkUnsafe(x, y, z);
        containedRegions.clear();
    }

    @Override
    public VoxelEntry toMutableEntry() {
        return this;
    }

    @Override
    public @Nullable VoxelEntry build() {
        return 0 >= voxelCount ? null : this;
    }

    public void upload() {
        var memory = this.memory;
        if (memory == null) return;

        int optimized = firstUpload ? 1 : 0;
        var intBuffer = memory.buffer().asIntBuffer();

        int[] data = UPLOAD_ARRAYS.get();

        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            final var oldEntry = getEntry(i);
            var newEntry = oldEntry != null ? oldEntry.build() : null;

            setEntry(i, oldEntry, newEntry);

            var oldEntryData = intBuffer.get(i);
            var newEntryData = newEntry == null ? VoxelModel.makeAirEntry(i) : VoxelEntry.toData(newEntry.entryData());

            int diff = VoxelEntry.entryDiff(oldEntryData, newEntryData);

            optimized |= diff;
            data[i] = !firstUpload && (diff == 0 && newEntry == null) ? oldEntryData : newEntryData;
        }

        if (optimized != 0)
            new OptimizeWrapper(data).optimize();

        intBuffer.put(0, data);

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
        return memory.buffer().getInt(index << 2);
    }

    @Override
    protected void set(int index, int value) {
        memory.buffer().putInt(index << 2, value);
    }

    @Override
    public void acceptBlock(int x, int y, int z, short region, BlockEntry entry) {
        insertBlock(x, y, z, region, entry);
    }

    @Override
    public void acceptVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        insertVoxel(x, y, z, region, normal, tint, textureData);
    }

    public static WorldVoxel create(
            int depth,
            WorldCompiler compiler,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap,
            Queue<WorldVoxel> uploadQueue
    ) {
        return switch(depth) {
            case BLOCK_DEPTH -> throw new IllegalArgumentException("Tried to create world voxel for block depth (0)");
            case CHUNK_DEPTH -> new ChunkVoxel(CHUNK_DEPTH, compiler, blockRegistry, heap, uploadQueue);
            case CHUNK_CONTAINER_DEPTH -> new ChunkContainerVoxel(CHUNK_CONTAINER_DEPTH, compiler, blockRegistry, heap, uploadQueue);

            default -> new WorldVoxel(depth, compiler, blockRegistry, heap, uploadQueue);
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
