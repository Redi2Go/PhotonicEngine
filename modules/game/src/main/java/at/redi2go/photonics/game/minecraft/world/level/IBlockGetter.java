package at.redi2go.photonics.game.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.world.level.block.IBlockEntity;
import org.jspecify.annotations.Nullable;

public interface IBlockGetter extends ILevelHeightAccessor {
    IBlockState ph$getBlockState(IBlockPos pos);

    @Nullable
    IBlockEntity ph$getBlockEntity(IBlockPos pos);
}
