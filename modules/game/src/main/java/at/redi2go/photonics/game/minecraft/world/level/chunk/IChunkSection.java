package at.redi2go.photonics.game.minecraft.world.level.chunk;

import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import org.joml.Vector3ic;

public interface IChunkSection {
    IBlockState ph$getBlockState(int x, int y, int z);

    default IBlockState ph$getBlockState(Vector3ic pos) {
        return ph$getBlockState(pos.x(), pos.y(), pos.z());
    }

    boolean ph$hasOnlyAir();

    IChunkSection ph$createCopy();

    interface ILightData {
        int ph$getLightLevel(int x, int y, int z);

        int ph$getLightLevel(int index);
    }
}
