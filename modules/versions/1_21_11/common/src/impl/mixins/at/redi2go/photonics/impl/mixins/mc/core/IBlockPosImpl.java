package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IBlockPos.class, remap = false)
public interface IBlockPosImpl {
    @Overwrite
    static IBlockPos zero() {
        return (IBlockPos) new BlockPos(0, 0, 0);
    }

    @Overwrite
    static IBlockPos of(int x, int y, int z) {
        return (IBlockPos) new BlockPos(x, y, z);
    }
}
