package at.redi2go.photonics.game.mc.world.level.block;

import at.redi2go.photonics.game.mc.core.IHolderLookup;
import at.redi2go.photonics.game.mc.nbt.ICompoundTag;

public interface IBlockEntity {
    ICompoundTag ph$saveWithFullMetadata(IHolderLookup.Provider provider);
}
