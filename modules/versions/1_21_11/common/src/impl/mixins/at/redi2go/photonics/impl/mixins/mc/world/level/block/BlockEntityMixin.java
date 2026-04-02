package at.redi2go.photonics.impl.mixins.mc.world.level.block;

import at.redi2go.photonics.api.mc.core.IHolderLookup;
import at.redi2go.photonics.api.mc.nbt.ICompoundTag;
import at.redi2go.photonics.api.mc.world.level.block.IBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
@SuppressWarnings("DataFlowIssue")
public abstract class BlockEntityMixin implements IBlockEntity {
    @Shadow
    public abstract CompoundTag saveWithFullMetadata(HolderLookup.Provider provider);

    @Override
    public ICompoundTag saveWithFullMetadata(IHolderLookup.Provider provider) {
        return (ICompoundTag) (Object) saveWithFullMetadata((HolderLookup.Provider) provider);
    }
}
