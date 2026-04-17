package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import at.redi2go.photonics.core.util.IntPacking;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;

public class WorldVoxel extends AbstractVoxelModel implements RtVoxel, Disposable {
    private final static int STATE_SHIFT = IntPacking.shiftFactor(0b11);
    private final static int STATE_LENGTH = RtVoxel.ENTRIES_SIZE >> STATE_SHIFT;
    private final static int STATE_SECTION_LENGTH = IntPacking.sectionLength(STATE_SHIFT);

    private final static int STATE_ENTRY_CREATED = 0b01;
    private final static int STATE_SET_VOXEL = 0b10;

    protected final WorldAllocator allocator;

    private final int[] state;

    private int voxelCount;
    private final Object[] data;

    private final int depth;
    private final int magnitude;

    private @Nullable MemoryView memory;
    private IntBuffer buffer;

    public WorldVoxel(int depth, WorldAllocator allocator) {
        super(SIZE_3);

        this.allocator = allocator;
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

    public boolean insert(
            int x, int y, int z,
            short region,
            int normal,
            TextureData textureData
    ) {
        if (!containsVoxel(x, y, z)) return false;

        var index = VoxelModel.toVoxelIndex(x, y, z, magnitude);
        var entry = data[index];

         var stateIndex = IntPacking.dataOffset(index, STATE_SHIFT);
         var newState = 0;

        if (entry == null) {
            entry = entryCreate(depth - 1);
            voxelCount++;

            newState |= STATE_ENTRY_CREATED;
        } else {
            var previous = entry;
            entry = entryMakeMutable(previous);

            if (entry != previous) {
                if (previous instanceof Disposable disposable)
                    disposable.close();

                newState |= STATE_ENTRY_CREATED;
            }
        }

        if (entryInsert(entry, x, y, z, region, normal, textureData))
            newState |= STATE_SET_VOXEL;

        state[stateIndex] = IntPacking.setValue(
                state[stateIndex],
                IntPacking.sectionIndex(index, STATE_SHIFT),
                newState,
                STATE_SHIFT
        );

        return newState != 0;
    }

    public void upload() {
        var firstUpload = false;
        var needsUpload = false;

        if (memory == null) {
            memory = allocator.allocate(ENTRIES_SIZE << 2);
            buffer = memory.buffer().asIntBuffer();

            firstUpload = needsUpload = true;
        }

        for (int s = 0; s < state.length; s++) {
            var sectionData = state[s];

            for (int o = 0; o < STATE_SECTION_LENGTH; o++) {
                int i = s + o;
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

                        needsSet = true;
                    }
                }

                if (needsSet) {
                    buffer.put(i, entry == null ? VoxelModel.makeAirEntry(i) : VoxelEntry.toData(entryBegin(entry)));
                    needsUpload = true;
                }

                if (state != 0) entryUpload(entry);
            }
        }

        if (needsUpload) {
            optimize();
            memory.upload();
        }
    }

    // entry methods

    protected Object entryCreate(int depth) {
        return WorldVoxel.create(depth, allocator);
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

    protected boolean entryInsert(Object entry, int x, int y, int z, short region, int normal, TextureData textureData) {
        return ((WorldVoxel) entry).insert(
                x, y, z,
                region,
                normal,
                textureData
        );
    }

    protected Object entryBuild(Object entry) {
        return entry;
    }

    protected void entryUpload(Object entry) {
        ((WorldVoxel) entry).upload();
    }

    protected void entryFree(Object entry) {
        if (entry instanceof Disposable disposable)
            disposable.close();
    }

    // Voxel model

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

    public static WorldVoxel create(int depth, WorldAllocator allocator) {
        return depth <= 1 ? new ChunkVoxel(depth, allocator) : new WorldVoxel(depth, allocator);
    }
}
