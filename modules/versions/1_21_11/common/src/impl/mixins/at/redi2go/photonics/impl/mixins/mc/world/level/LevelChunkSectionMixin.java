package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.chunk.IChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements IChunkSection {
    @Shadow
    public abstract BlockState shadow$getBlockState(int x, int y, int z);

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return (IBlockState) shadow$getBlockState(x, y, z);
    }

    @Override
    public IChunkSection createCopy() {
        return (IChunkSection) LevelChunkSectionInvoker.copySection((LevelChunkSection) (Object) this);
    }
}
