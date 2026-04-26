package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IHolderLookup;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderLookup.Provider.class)
public interface HolderLookupProviderMixin extends IHolderLookup.Provider {
}
