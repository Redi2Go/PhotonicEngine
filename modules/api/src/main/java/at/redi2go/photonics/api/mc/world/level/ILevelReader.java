package at.redi2go.photonics.api.mc.world.level;

import at.redi2go.photonics.api.mc.core.IRegistryAccess;

public interface ILevelReader extends IBlockAndTintGetter {
    IRegistryAccess registryAccess();

    static ILevelReader createFacade(IBlockState blockState) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
