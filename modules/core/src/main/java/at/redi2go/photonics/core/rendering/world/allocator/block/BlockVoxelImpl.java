package at.redi2go.photonics.core.rendering.world.allocator.block;

import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.allocator.AbstractHashedObject;
import at.redi2go.photonics.core.rendering.world.allocator.BufferWorldAllocator;
import at.redi2go.photonics.core.rendering.world.block.BlockVoxel;
import at.redi2go.photonics.core.util.IntPacking;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.nio.IntBuffer;

public class BlockVoxelImpl extends AbstractHashedObject implements BlockVoxel {
    private final int shift;
    private final long hashCode;

    private IntBuffer buffer = null;

    public BlockVoxelImpl(
            BufferWorldAllocator allocator,
            int shift,
            long hashCode
    ) {
        super(allocator);

        this.shift = shift;
        this.hashCode = hashCode;
    }

    @Override
    protected long hash() {
        return hashCode;
    }

    public void allocate(int[] data) {
        initMemory(data.length * 4);
        buffer = memory.buffer().asIntBuffer();

        buffer.put(data);
    }

    public int shift() {
        return shift;
    }

    public IntBuffer buffer() {
        return buffer;
    }

    @Override
    public Vector3ic size() {
        return RtVoxel.SIZE_3;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return VoxelModel.contains(x, y, z, RtVoxel.SIDE_LENGTH, RtVoxel.SIDE_LENGTH, RtVoxel.SIDE_LENGTH);
    }

    @Override
    public int get(int x, int y, int z) {
        int realIndex = VoxelModel.toVoxelIndex(x, y, z);

        int offset = IntPacking.dataOffset(realIndex, shift);
        int sectionIndex = IntPacking.sectionIndex(realIndex, shift);

        return IntPacking.getValue(buffer.get(offset), sectionIndex, shift);
    }

    @Override
    protected void dispose() {
        // Nothing to release
    }
}
