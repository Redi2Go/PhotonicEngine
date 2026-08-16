package at.redi2go.photonics.game.minecraft.core.registries;

import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;

public interface Registries {
    static IHolderLookup<IBlock> block() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
