package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.game.mc.world.level.IBlockState;
import at.redi2go.photonics.game.mc.world.level.chunk.IChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements IChunkSection {
    @Shadow
    public abstract BlockState getBlockState(int x, int y, int z);

    @Shadow
    public abstract boolean hasOnlyAir();

    @Override
    public IBlockState ph$getBlockState(int x, int y, int z) {
        return (IBlockState) getBlockState(x, y, z);
    }

    @Override
    public boolean ph$hasOnlyAir() {
        return hasOnlyAir();
    }

    @Override
    public IChunkSection ph$createCopy() {
        return (IChunkSection) LevelChunkSectionInvoker.copySection((LevelChunkSection) (Object) this);
    }
}
