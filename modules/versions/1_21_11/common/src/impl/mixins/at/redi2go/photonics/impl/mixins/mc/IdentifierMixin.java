package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Identifier.class)
public abstract class IdentifierMixin implements Id {
    @Shadow
    public abstract String getNamespace();

    @Shadow
    public abstract String getPath();

    @Override
    public String ph$namespace() {
        return getNamespace();
    }

    @Override
    public String ph$path() {
        return getPath();
    }
}
