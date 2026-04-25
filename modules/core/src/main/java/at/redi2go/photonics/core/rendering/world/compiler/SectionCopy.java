package at.redi2go.photonics.core.rendering.world.compiler;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import at.redi2go.photonics.core.model.VoxelModel;
import org.joml.Vector3i;

public class SectionCopy implements IChunkSection {
    private final Vector3i pos;
    private final IBlockState[] blockStates;

    public SectionCopy(Vector3i pos, IChunkSection section) {
        this.pos = pos;
        this.blockStates = new IBlockState[IChunkSection.SECTION_SIZE];

        Vector3i coord = new Vector3i();
        for (int i = 0; i < IChunkSection.SECTION_SIZE; i++) {
            VoxelModel.fromVoxelIndex(i, coord);
            blockStates[i] = section.getBlockState(coord);
        }
    }

    public Vector3i pos() {
        return pos;
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return blockStates[VoxelModel.toVoxelIndex(x, y, z)];
    }

    @Override
    public boolean hasOnlyAir() {
        return false;
    }
}
