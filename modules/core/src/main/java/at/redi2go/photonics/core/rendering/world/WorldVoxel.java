package at.redi2go.photonics.core.rendering.world;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;

import java.nio.IntBuffer;

public class WorldVoxel extends AbstractVoxelModel implements RtVoxel, Disposable {
    private static final int MASKS_SIZE = (int) Math.ceil(ENTRIES_SIZE/ 64d);

    protected final WorldAllocator allocator;
    private final MemoryView memory;
    private final IntBuffer buffer;

    private boolean needsOptimization = false;
    private final long[] modifiedMasks;

    private final int depth;
    private final int magnitude;

    public WorldVoxel(int depth, WorldAllocator allocator) {
        super(SIZE_3);

        this.allocator = allocator;
        this.memory = allocator.allocate(ENTRIES_SIZE << 2, this);
        this.buffer = memory.buffer().asIntBuffer();

        this.modifiedMasks = new long[MASKS_SIZE];

        this.depth = depth;
        this.magnitude = depth << 2;
    }

    @Override
    public int blockSideLength() {
        return 1 << magnitude;
    }

    @Override
    public int voxelSideLength() {
        return SIZE << magnitude;
    }

    @Override
    protected int get(int index) {
        return buffer.get(index);
    }

    @Override
    protected void set(int index, int value) {
        buffer.put(index, value);
    }

    protected Object createVoxel(int depth) {
        return create(depth, allocator);
    }

    protected int getVoxelPtr(Object voxel) {
        return MemoryView.intBufferBegin(((WorldVoxel) voxel).memory);
    }

    protected Object createMutableCopy(Object voxel) {
        return voxel;
    }

    protected boolean setVoxelData(
            Object voxel,
            int x, int y, int z,
            int region,
            int normal,
            TextureData textureData
    ) {
        return ((WorldVoxel) voxel).insert(
                x, y, z,
                region,
                normal,
                textureData
        );
    }

    protected void finalizeVoxel(int index, Object voxel) {

    }

    public boolean insert(
            int x, int y, int z,
            int region,
            int normal,
            TextureData textureData
    ) {
        boolean created = false;
        Object voxel;

        var index = VoxelModel.toVoxelIndex(
                (x >> magnitude) & 15,
                (y >> magnitude) & 15,
                (z >> magnitude) & 15
        );

        int entry = get(index);
        if (VoxelEntry.isAir(entry)) {
            voxel = createVoxel(depth - 1);
            set(index, VoxelEntry.toData(getVoxelPtr(voxel)));

            needsOptimization = true;
        } else {
            var previous = allocator.getOwner(VoxelEntry.getData(entry));
            if (previous instanceof ReferencedObject obj)
                obj.release();

            voxel = createMutableCopy(previous);
            created = true;
        }

        if (setVoxelData(voxel, x, y, z, region, normal, textureData) || created) {
            int maskIndex = index >> 6;
            long mask = modifiedMasks[maskIndex];

            modifiedMasks[maskIndex] = mask | (1L << (index & 63));
        }

        return true;
    }

    @Override
    public void close() {
        memory.close();
    }

    public static WorldVoxel create(int depth, WorldAllocator allocator) {
        if (depth <= 1)
            return new ChunkVoxel(depth, allocator);

        return new WorldVoxel(depth, allocator);
    }
}
