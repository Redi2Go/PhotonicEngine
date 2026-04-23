package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.bakery.BlockConsumer;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.rendering.world.compiler.WorldCompiler;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3ic;

import java.util.Queue;

public class WorldVoxel extends AbstractVoxelModel implements VoxelEntry, RtVoxel, BlockConsumer, VoxelConsumer {
    private static final ThreadLocal<int[]> UPLOAD_ARRAYS = ThreadLocal.withInitial(() -> new int[RtVoxel.ENTRIES_SIZE]);

    private final int depth;
    protected final BlockRegistry blockRegistry;
    protected final IGpuBufferHeap heap;
    protected final Queue<WorldVoxel> uploadQueue;

    protected final VoxelEntry[] voxelData = new VoxelEntry[RtVoxel.ENTRIES_SIZE];
    private int voxelCount = 0;

    private final MemoryView memory;
    private boolean updateRequested = false;
    protected boolean optimized = false;

    public WorldVoxel(
            int depth,
            BlockRegistry blockRegistry,
            IGpuBufferHeap heap,
            Queue<WorldVoxel> uploadQueue
    ) {
        super(SIZE_3);

        this.depth = depth;
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
        optimized = false;
    }

    protected void decVoxelCount() {
        voxelCount--;
        optimized = false;
    }

    // Voxel entry methods

    protected VoxelEntry newMutableEntry(int depth) {
        return depth > 1 ? new WorldVoxel(depth, blockRegistry, heap, uploadQueue) : new ChunkVoxel(depth, blockRegistry, heap, uploadQueue);
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
        getMutableEntry(x, y, z, region)
                .insertVoxel(x, y, z, region, normal, tint, textureData);
    }

    @Override
    public void insertBlock(
            int x, int y, int z,
            short region,
            BlockEntry block
    ) {
        getMutableEntry(x, y, z, region)
                .insertBlock(x, y, z, region, block);
    }

    @Override
    public @Nullable VoxelEntry removeRegions(ShortSet regions) {
        // TODO

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
        int[] data = UPLOAD_ARRAYS.get();

        for (int i = 0; i < voxelData.length; i++) {
            var entry = voxelData[i];

            buildEntry: {
                if (entry == null) break buildEntry;

                var previous = entry;
                entry = entry.build();

                if (entry != previous) {
                    previous.close();

                    voxelData[i] = entry;

                    if (entry == null) decVoxelCount();
                }
            }

            data[i] = entry == null ? VoxelModel.makeAirEntry(i) : VoxelEntry.toData(entry.entryData());
        }

        if (!optimized) {
            new ModelWrapper(data).optimize();
            optimized = true;
        }

        memory.buffer().asIntBuffer().put(0, data);

        memory.upload();
        updateRequested = false;
    }

    @Override
    public void close() {
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
