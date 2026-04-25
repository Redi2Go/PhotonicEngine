package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.world.level.chunk.IChunkAccess;
import org.jetbrains.annotations.Nullable;

public interface ILevel extends ILevelReader {
    @Nullable IChunkAccess getChunkOrNull(int x, int y);
}
