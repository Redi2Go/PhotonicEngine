package at.redi2go.photonics.api.mc.world.level.chunk;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public interface IChunkSection {
    int SECTION_WIDTH = 16;
    int SECTION_HEIGHT = 16;
    int SECTION_SIZE = 4096;

    IBlockState getBlockState(int x, int y, int z);

    default IBlockState getBlockState(Vector3ic pos) {
        return getBlockState(pos.x(), pos.y(), pos.z());
    }

    boolean hasOnlyAir();
}
