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
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.util.IntPacking;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;

public class WorldVoxel extends AbstractVoxelModel implements RtVoxel, Disposable, BlockConsumer, VoxelConsumer {
    private final static int STATE_SHIFT = IntPacking.shiftFactor(0b11);
    private final static int STATE_LENGTH = RtVoxel.ENTRIES_SIZE >> STATE_SHIFT;
    private final static int STATE_SECTION_LENGTH = IntPacking.sectionLength(STATE_SHIFT);

    private final static int STATE_ENTRY_CREATED = 0b01;
    private final static int STATE_SET_VOXEL = 0b10;

    protected final BlockRegistry registry;

    private final int[] state;

    private int voxelCount;
    private final Object[] data;

    private final int depth;
    protected final int magnitude;

    private @Nullable MemoryView memory;
    private IntBuffer buffer;

    public WorldVoxel(int depth, BlockRegistry registry) {
        super(SIZE_3);

        this.registry = registry;
        this.depth = depth;

        this.magnitude = depth << 2;

        this.state = new int[STATE_LENGTH];

        this.voxelCount = 0;
        this.data = new Object[RtVoxel.ENTRIES_SIZE];
    }

    @Override
    public int blockSideLength() {
        return 1 << magnitude;
    }

    @Override
    public int voxelSideLength() {
        return SIDE_LENGTH << magnitude;
    }

    public boolean insertVoxel(
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    ) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude);
        var entry = data[index];

         var stateIndex = IntPacking.dataOffset(index, STATE_SHIFT);
         var newState = 0;

        if (entry == null) {
            entry = entryCreate(depth - 1);
            data[index] = entry;

            voxelCount++;
            newState |= STATE_ENTRY_CREATED;
        } else {
            var previous = entry;
            entry = entryMakeMutable(previous);

            if (entry != previous) {
                if (previous instanceof Disposable disposable)
                    disposable.close();

                data[index] = entry;
                newState |= STATE_ENTRY_CREATED;
            }
        }

        if (entryInsert(entry, x, y, z, region, normal, tint, textureData))
            newState |= STATE_SET_VOXEL;

        int sectionData = state[stateIndex];
        state[stateIndex] = IntPacking.setValue(
                sectionData,
                IntPacking.sectionIndex(index, STATE_SHIFT),
                IntPacking.getValue(sectionData, index, STATE_SHIFT) | newState,
                STATE_SHIFT
        );

        return true;
    }

    public void insertBlock(
            int x, int y, int z,
            short region,
            BlockEntry block
    ) {
        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude);
        var entry = data[index];

        var stateIndex = IntPacking.dataOffset(index, STATE_SHIFT);
        var newState = 0;

        var newEntry = entryInsertBlock(entry, block);
        if (entry == null) {
            voxelCount++;
            newState |= STATE_ENTRY_CREATED;
        } else entryFree(entry);

        data[index] = newEntry;
        if (newEntry instanceof WorldVoxel voxel)
            voxel.insertBlock(x, y, z, region, block);

        newState |= STATE_SET_VOXEL;

        int sectionData = state[stateIndex];
        state[stateIndex] = IntPacking.setValue(
                sectionData,
                IntPacking.sectionIndex(index, STATE_SHIFT),
                IntPacking.getValue(sectionData, index, STATE_SHIFT) | newState,
                STATE_SHIFT
        );
    }

    public void upload(IGpuBufferHeap allocator) {
        boolean firstUpload, needsUpload;
        needsUpload = firstUpload = ensureAllocated(allocator);

        for (int s = 0; s < state.length; s++) {
            var sectionData = state[s];

            for (int o = 0; o < STATE_SECTION_LENGTH; o++) {
                int i = (s << STATE_SHIFT) + o;
                int state = IntPacking.getValue(sectionData, o, STATE_SHIFT);
                var entry = data[i];

                var needsSet = firstUpload;

                if ((state & STATE_ENTRY_CREATED) != 0) needsSet = true;

                handleEntry: {
                    if (entry == null) break handleEntry;

                    var previous = entry;
                    entry = entryBuild(entry);

                    if (entry != previous) {
                        entryFree(previous);
                        data[i] = entry;

                        needsSet = true;
                    }

                    if (entryIsEmpty(entry)) {
                        entryFree(entry);

                        data[i] = null;
                        entry = null;

                        voxelCount--;

                        needsSet = true;
                    }
                }

                if (state != 0 && entry != null) entryUpload(entry, allocator);

                if (needsSet) {
                    buffer.put(i, entry == null ? VoxelModel.makeAirEntry(i) : VoxelEntry.toData(entryBegin(entry)));
                    needsUpload = true;
                }
            }

            state[s] = 0;
        }

        if (needsUpload) {
            optimize();
            memory.upload();
        }
    }

    // entry methods

    protected Object entryCreate(int depth) {
        return WorldVoxel.create(depth, registry);
    }

    protected boolean entryIsEmpty(Object entry) {
        return ((WorldVoxel) entry).voxelCount == 0;
    }

    protected int entryBegin(Object entry) {
        var obj = ((WorldVoxel) entry);
        obj.checkAllocated();

        return MemoryView.intBufferBegin(obj.memory);
    }

    protected Object entryMakeMutable(Object entry) {
        return entry;
    }

    protected boolean entryInsert(
            Object entry,
            int x, int y, int z,
            short region,
            int normal,
            int tint,
            TextureData textureData
    ) {
        return ((WorldVoxel) entry).insertVoxel(
                x, y, z,
                region,
                normal,
                tint,
                textureData
        );
    }

    protected Object entryInsertBlock(
            Object previousEntry,
            BlockEntry entry
    ) {
        return previousEntry != null ? previousEntry : entryCreate(depth - 1);
    }

    protected Object entryBuild(Object entry) {
        return entry;
    }

    protected void entryUpload(Object entry, IGpuBufferHeap allocator) {
        ((WorldVoxel) entry).upload(allocator);
    }

    protected void entryFree(Object entry) {
        if (entry instanceof Disposable disposable)
            disposable.close();
    }

    // Voxel model

    public boolean ensureAllocated(IGpuBufferHeap allocator) {
        if (memory == null) {
            memory = allocator.allocate(ENTRIES_SIZE << 2);
            buffer = memory.buffer().asIntBuffer();

            return true;
        }

        return false;
    }

    public boolean isAllocated() {
        return memory != null;
    }

    public int begin() {
        return MemoryView.intBufferBegin(memory);
    }

    private void checkAllocated() {
        if (memory == null)
            throw new IllegalStateException("Cannot access voxel data before allocation");
    }

    @Override
    protected int get(int index) {
        checkAllocated();

        return buffer.get(index);
    }

    @Override
    protected void set(int index, int value) {
        checkAllocated();

        buffer.put(index, value);
    }

    @Override
    public void close() {
        if (memory != null) memory.close();
    }

    public static WorldVoxel create(int depth, BlockRegistry registry) {
        return depth <= 1 ? new ChunkVoxel(depth, registry) : new WorldVoxel(depth, registry);
    }

    @Override
    public void acceptBlock(int x, int y, int z, short region, BlockEntry entry) {
        insertBlock(x, y, z, region, entry);
    }

    @Override
    public void acceptVoxel(int x, int y, int z, short region, int normal, int tint, TextureData textureData) {
        insertVoxel(x, y, z, region, normal, tint, textureData);
    }
}
