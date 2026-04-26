package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IHolderSet;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderSet.class)
public interface HolderSetMixin<T> extends IHolderSet<T> {
}
