package at.redi2go.photonics.game.mc.world.level;

import at.redi2go.photonics.game.mc.core.IBlockPos;
import at.redi2go.photonics.game.mc.world.level.block.IBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IBlockGetter extends ILevelHeightAccessor {
    IBlockState ph$getBlockState(IBlockPos pos);

    @Nullable
    IBlockEntity ph$getBlockEntity(IBlockPos pos);
}
