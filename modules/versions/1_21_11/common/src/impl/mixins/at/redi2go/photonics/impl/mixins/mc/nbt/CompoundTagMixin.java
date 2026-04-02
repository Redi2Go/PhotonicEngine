package at.redi2go.photonics.impl.mixins.mc.nbt;

import at.redi2go.photonics.api.mc.nbt.ICompoundTag;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin implements ICompoundTag {

}
