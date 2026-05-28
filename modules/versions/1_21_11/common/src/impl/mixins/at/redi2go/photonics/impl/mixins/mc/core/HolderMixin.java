package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IHolder;
import at.redi2go.photonics.api.mc.core.IHolderLookup;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Holder.class)
public interface HolderMixin<T> extends IHolder<T> {
    @Shadow
    T value();

    @Override
    default T ph$value() {
        return value();
    }
}
