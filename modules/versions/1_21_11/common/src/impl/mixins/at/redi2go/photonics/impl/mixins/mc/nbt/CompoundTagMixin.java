package at.redi2go.photonics.impl.mixins.mc.nbt;

import at.redi2go.photonics.api.mc.nbt.ICompoundTag;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin implements ICompoundTag{
    @Shadow
    public abstract boolean isEmpty();

    @Override
    public boolean ph$isEmpty() {
        return isEmpty();
    }
}
