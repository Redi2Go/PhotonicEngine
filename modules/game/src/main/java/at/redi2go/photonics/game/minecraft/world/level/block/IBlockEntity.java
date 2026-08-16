package at.redi2go.photonics.game.minecraft.world.level.block;

import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;

public interface IBlockEntity {
    ICompoundTag ph$saveWithFullMetadata(IHolderLookup.Provider provider);
}
