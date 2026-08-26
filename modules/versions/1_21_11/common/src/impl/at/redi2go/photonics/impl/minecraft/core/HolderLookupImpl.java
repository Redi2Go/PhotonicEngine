package at.redi2go.photonics.impl.minecraft.core;

import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HolderLookup.class)
public interface HolderLookupImpl<T> extends IHolderLookup<T> {
    @Mixin(HolderLookup.Provider.class)
    interface ProviderImpl extends IHolderLookup.Provider {

    }
}
