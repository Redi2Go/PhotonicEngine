package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.block.IBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin extends IBlockGetter {
    @Shadow BlockState shadow$getBlockState(BlockPos pos);

    @Override
    default IBlockState getBlockState(IBlockPos pos) {
        return (IBlockState) shadow$getBlockState((BlockPos) pos);
    }

    @Shadow BlockEntity shadow$getBlockEntity(BlockPos pos);

    @Override
    default @Nullable IBlockEntity getBlockEntity(IBlockPos pos) {
        return (IBlockEntity) shadow$getBlockEntity((BlockPos) pos);
    }
}
