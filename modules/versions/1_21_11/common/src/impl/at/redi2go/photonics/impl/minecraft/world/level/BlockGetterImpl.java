package at.redi2go.photonics.impl.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.world.level.IBlockGetter;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import at.redi2go.photonics.game.minecraft.world.level.block.IBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockGetter.class)
public interface BlockGetterImpl extends IBlockGetter {
    @Shadow
    BlockState getBlockState(BlockPos blockPos);

    @Shadow
    @Nullable BlockEntity getBlockEntity(BlockPos blockPos);

    @Override
    default IBlockState ph$getBlockState(IBlockPos pos) {
        return (IBlockState) getBlockState((BlockPos) pos);
    }

    @Override
    default @Nullable IBlockEntity ph$getBlockEntity(IBlockPos pos) {
        return (IBlockEntity) getBlockEntity((BlockPos) pos);
    }
}
