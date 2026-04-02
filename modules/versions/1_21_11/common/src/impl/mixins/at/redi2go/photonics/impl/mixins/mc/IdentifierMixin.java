package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Identifier.class)
public abstract class IdentifierMixin implements Id {
}
