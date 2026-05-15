package at.redi2go.photonics.core.rendering;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import at.redi2go.photonics.core.model.VoxelModel;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3i;

import java.util.function.BiConsumer;

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

    public Vector3i blockPos() {
        return pos.mul(16, new Vector3i());
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return blockStates[VoxelModel.toVoxelIndex(x, y, z)];
    }

    @Override
    public boolean hasOnlyAir() {
        return false;
    }

    public void forEachBlock(TriConsumer<Vector3i, IBlockPos, IBlockState> blockConsumer) {
        Vector3i chunkOffset = new Vector3i();
        Vector3i sectionBlockPos = blockPos();

        for (int px = 0; px < 16; px++) {
            for (int py = 0; py < 16; py++) {
                for (int pz = 0; pz < 16; pz++) {
                    IBlockPos blockPos = IBlockPos.of(
                            sectionBlockPos.x + px,
                            sectionBlockPos.y + py,
                            sectionBlockPos.z + pz
                    );

                    chunkOffset.set(px, py, pz);
                    blockConsumer.accept(chunkOffset, blockPos, getBlockState(px, py, pz));
                }
            }
        }
    }

    public long computeSectionHash() {
        final long[] hash = {0};

        forEachBlock((ignored, ignored1, block) ->
                hash[0] = hash[0] * 31 + block.hashCode()
        );

        return hash[0];
    }
}
