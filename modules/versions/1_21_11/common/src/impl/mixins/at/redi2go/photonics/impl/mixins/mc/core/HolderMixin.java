package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IHolder;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Holder.class)
public interface HolderMixin<T> extends IHolder<T> {
}
