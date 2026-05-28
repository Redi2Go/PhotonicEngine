package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.joml.Vector3i;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i implements IBlockPos {
    private BlockPosMixin(int i, int j, int k) {
        super(i, j, k);
    }

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
}
