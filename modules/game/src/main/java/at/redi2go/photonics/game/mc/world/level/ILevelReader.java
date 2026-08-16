package at.redi2go.photonics.game.mc.world.level;

import at.redi2go.photonics.game.mc.core.IRegistryAccess;

public interface ILevelReader extends IBlockAndTintGetter {
    IRegistryAccess ph$registryAccess();

    static ILevelReader createFacade(IBlockState blockState) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
