package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.world.level.chunk.IChunkAccess;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin implements IChunkAccess {
    @Shadow
    public abstract LevelChunkSection[] getSections();

    @Override
    public IChunkSection[] ph$sections() {
        return (IChunkSection[]) getSections();
    }
}
