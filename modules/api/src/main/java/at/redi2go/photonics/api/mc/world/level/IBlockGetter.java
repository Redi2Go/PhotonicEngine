package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.block.IBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IBlockGetter extends ILevelHeightAccessor {
    IBlockState getBlockState(IBlockPos pos);

    @Nullable
    IBlockEntity getBlockEntity(IBlockPos pos);
}
