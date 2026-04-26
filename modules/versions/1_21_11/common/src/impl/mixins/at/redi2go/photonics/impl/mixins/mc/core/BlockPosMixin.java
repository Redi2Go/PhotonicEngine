package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i implements IBlockPos {
    private BlockPosMixin(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    public int x() {
        return getX();
    }

    @Override
    public int y() {
        return getY();
    }

    @Override
    public int z() {
        return getZ();
    }
}
