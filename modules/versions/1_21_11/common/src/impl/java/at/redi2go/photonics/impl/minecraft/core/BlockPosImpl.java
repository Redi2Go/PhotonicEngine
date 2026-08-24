package at.redi2go.photonics.impl.minecraft.core;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockPos.class)
public abstract class BlockPosImpl extends Vec3i implements IBlockPos {
    @Override
    public int ph$x() {
        return getX();
    }

    @Override
    public int ph$y() {
        return getY();
    }

    @Override
    public int ph$z() {
        return getZ();
    }

    @Override
    public IBlockPos ph$offset(int x, int y, int z) {
        return (IBlockPos) offset(x, y, z);
    }

    @Mixin(IBlockPos.class)
    public interface StaticMethods {
        @Overwrite
        static IBlockPos zero() {
            return (IBlockPos) BlockPos.ZERO;
        }

        @Overwrite
        static IBlockPos of(int x, int y, int z) {
            return (IBlockPos) new BlockPos(x, y, z);
        }
    }

    private BlockPosImpl(int i, int j, int k) {
        super(i, j, k);
    }
}
