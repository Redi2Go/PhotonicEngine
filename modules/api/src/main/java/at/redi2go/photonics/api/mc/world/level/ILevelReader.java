package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.core.IRegistryAccess;
import at.redi2go.photonics.api.mc.world.level.block.IBlockEntity;
import org.jetbrains.annotations.Nullable;

public interface ILevelReader extends IBlockGetter {
    IRegistryAccess registryAccess();

    static ILevelReader createFacade(IBlockState blockState) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
