package at.redi2go.photonics.game.mc.world.level;

import at.redi2go.photonics.game.mc.core.IBlockPos;
import at.redi2go.photonics.game.mc.world.level.chunk.IChunkAccess;
import org.jetbrains.annotations.Nullable;

public interface ILevel extends ILevelReader {
    @Nullable IChunkAccess ph$getChunkOrNull(int x, int y);

    int ph$getSkylightValue(IBlockPos pos);
}
