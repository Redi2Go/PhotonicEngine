package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.bakery.BlockConsumer;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
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

    protected final VoxelEntry[] voxelData = new VoxelEntry[RtVoxel.ENTRIES_SIZE];
    protected final ShortSet containedRegions = new ShortOpenHashSet();
    private int voxelCount = 0;

    private boolean firstUpload = true;
    private boolean closed = false;
    private final MemoryView memory;
    private boolean updateRequested = false;

    public WorldVoxel(
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

    protected void requestUpload() {
        if (updateRequested) return;

        uploadQueue.offer(this);
        updateRequested = true;
    }

    protected void incVoxelCount() {
        voxelCount++;
    }

    protected void decVoxelCount() {
        voxelCount--;
    }

    // Voxel entry methods

    protected VoxelEntry newMutableEntry(int depth) {
        return depth > CHUNK_DEPTH ? new WorldVoxel(depth, compiler, blockRegistry, heap, uploadQueue) : new ChunkVoxel(depth, compiler, blockRegistry, heap, uploadQueue);
    }

    @Override
    public int entryData() {
        return MemoryView.intBufferBegin(memory);
    }

    private VoxelEntry getMutableEntry(
            int x, int y, int z,
            short region
    ) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = voxelData[index];

        if (entry == null) {
            entry = newMutableEntry(depth - 1);
            voxelData[index] = entry;
            incVoxelCount();

            requestUpload();
        } else {
            var previous = entry;
            entry = entry.toMutableEntry();

            if (entry != previous) {
                previous.close();

                voxelData[index] = entry;
                requestUpload();
            }
        }

        return entry;
    }

    @Override
    public void insertVoxel(
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    ) {
        containedRegions.add(region);

        getMutableEntry(x, y, z, region)
                .insertVoxel(x, y, z, region, normal, tint, textureData);
    }

    @Override
    public void insertBlock(
            int x, int y, int z,
            short region,
            BlockEntry block
    ) {
        containedRegions.add(region);

        getMutableEntry(x, y, z, region)
                .insertBlock(x, y, z, region, block);
    }

    public void removeEmptyVoxels() {
        if (depth <= CHUNK_DEPTH) return;

        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            var entry = voxelData[i];
            if (entry == null) continue;
            if (!(entry instanceof WorldVoxel voxel)) continue;

            voxel.removeEmptyVoxels();
            if (voxel.voxelCount > 0) continue;

            entry.close();
            voxelData[i] = null;

            decVoxelCount();
            requestUpload();
        }
    }

    private boolean containsAnyRegion(ShortSet regions) {
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

    public void insertChunk(int x, int y, int z, ChunkVoxel chunk) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = voxelData[index];

        if (depth == CHUNK_CONTAINER_DEPTH) {
            if (entry == chunk) {
                Photonics.LOGGER.warn("set chunk twice");

                containedRegions.addAll(chunk.containedRegions);
                return;
            }

            if (entry != null)
                throw new IllegalStateException("Duplicate chunk");

            voxelData[index] = chunk;

            incVoxelCount();
            requestUpload();
        } else {
            if (entry == null) {
                entry = newMutableEntry(depth - 1);
                voxelData[index] = entry;

                incVoxelCount();
                requestUpload();
            }

            ((WorldVoxel) entry).insertChunk(x, y, z, chunk);
        }

        containedRegions.addAll(chunk.containedRegions);
    }

    public void removeChunk(
            int x,
            int y,
            int z,
            short region,
            boolean freeEmptyVoxels
    ) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude());
        var entry = voxelData[index];

        if (depth == CHUNK_CONTAINER_DEPTH) {
            voxelData[index] = null;

            if (entry != null) {
                decVoxelCount();
                requestUpload();

                if (freeEmptyVoxels)
                    entry.close();
            }
        } else if (entry != null) {
            var worldVoxel = ((WorldVoxel) entry);

            worldVoxel.removeChunk(x, y, z, region, freeEmptyVoxels);
            if (freeEmptyVoxels && worldVoxel.voxelCount == 0) {
                worldVoxel.close();
                voxelData[index] = null;

                decVoxelCount();
                requestUpload();
            }
        }

        containedRegions.remove(region);
    }

    @Override
    public @Nullable VoxelEntry removeRegions(ShortSet regions) {
        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            var entry = voxelData[i];
            if (entry == null) continue;
            if (entry instanceof BlockEntry.Builder) {
                Photonics.LOGGER.warn("HOW");
            }

            if (entry instanceof WorldVoxel worldVoxel && !worldVoxel.containsAnyRegion(regions))
                continue;

            var newEntry = entry.removeRegions(regions);
            if (entry != newEntry) {
                entry.close();

                voxelData[i] = newEntry;

                if (newEntry == null) decVoxelCount();

                requestUpload();
            }
        }

        containedRegions.removeAll(regions);

        return this;
    }

    @Override
    public VoxelEntry toMutableEntry() {
        return this;
    }

    @Override
    public @Nullable VoxelEntry build() {
        return voxelCount == 0 ? null : this;
    }

    public void upload() {
        if (closed) return;

        int optimized = firstUpload ? 1 : 0;
        var intBuffer = memory.buffer().asIntBuffer();

        int[] data = UPLOAD_ARRAYS.get();

        for (int i = 0; i < voxelData.length; i++) {
            var entry = voxelData[i];

            buildEntry:
            {
                if (entry == null) break buildEntry;

                var previous = entry;
                entry = entry.build();

                if (entry != previous) {
                    previous.close();

                    voxelData[i] = entry;

                    if (entry == null) decVoxelCount();
                }
            }

            var previousEntry = intBuffer.get(i);
            var newEntry = entry == null ? VoxelModel.makeAirEntry(i) : VoxelEntry.toData(entry.entryData());

            int diff = VoxelEntry.entryDiff(previousEntry, newEntry);

            optimized |= diff;
            data[i] = !firstUpload && (diff == 0 && entry == null) ? previousEntry : newEntry;
        }

        if (optimized != 0)
            new ModelWrapper(data).optimize();

        intBuffer.put(0, data);

        memory.upload();

        updateRequested = false;
        firstUpload = false;
    }

    @Override
    public void close() {
        closed = true;
        memory.close();
    }

    // VoxelModel methods

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

    // Consumer methods

    @Override
    public void acceptBlock(int x, int y, int z, short region, BlockEntry entry) {
        insertBlock(x, y, z, region, entry);
    }

    @Override
    public void acceptVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        insertVoxel(x, y, z, region, normal, tint, textureData);
    }

    private static class ModelWrapper extends AbstractVoxelModel {
        private final int[] data;

        public ModelWrapper(int[] data) {
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
