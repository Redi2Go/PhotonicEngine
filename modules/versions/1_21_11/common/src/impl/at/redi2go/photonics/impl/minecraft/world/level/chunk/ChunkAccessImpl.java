package at.redi2go.photonics.impl.minecraft.world.level.chunk;

import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkAccess;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkSection;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessImpl implements IChunkAccess {
    @Shadow @Final protected LevelChunkSection[] sections;

    @Override
    public IChunkSection[] ph$sections() {
        return (IChunkSection[]) sections;
    }
}
