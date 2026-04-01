package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.core.IRegistryAccess;
import at.redi2go.photonics.api.mc.world.level.block.IBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface ILevelReader {
    IRegistryAccess registryAccess();

    IBlockState getBlockState(IBlockPos pos);

    @Nullable
    IBlockEntity getBlockEntity(IBlockPos pos);

    static ILevelReader createFacade(IBlockState blockState) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
