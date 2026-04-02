package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.api.mc.core.IRegistryAccess;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryAccess.class)
public interface RegistryAccessMixin extends IRegistryAccess {
}
