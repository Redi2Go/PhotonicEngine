package at.redi2go.photonics.impl.minecraft.core;

import at.redi2go.photonics.game.minecraft.IIdentifier;
import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryAccess.class)
public interface RegistryAccessImpl extends IHolderLookup.Provider {
}
