package at.redi2go.photonics.game.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkAccess;
import org.jetbrains.annotations.Nullable;

public interface ILevel extends ILevelReader {
    @Nullable IChunkAccess ph$getChunkOrNull(int x, int y);

    int ph$getSkylightValue(IBlockPos pos);
}
