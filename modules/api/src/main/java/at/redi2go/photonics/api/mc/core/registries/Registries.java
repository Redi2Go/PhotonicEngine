package at.redi2go.photonics.api.mc.core.registries;

import at.redi2go.photonics.api.mc.core.IHolderLookup;
import at.redi2go.photonics.api.mc.world.level.IBlock;

public interface Registries {
    static IHolderLookup<IBlock> block() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
