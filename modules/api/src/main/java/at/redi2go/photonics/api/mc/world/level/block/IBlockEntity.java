package at.redi2go.photonics.api.mc.world.level.block;

import at.redi2go.photonics.api.mc.core.IHolderLookup;
import at.redi2go.photonics.api.mc.nbt.ICompoundTag;

public interface IBlockEntity {
    ICompoundTag saveWithFullMetadata(IHolderLookup.Provider provider);
}
