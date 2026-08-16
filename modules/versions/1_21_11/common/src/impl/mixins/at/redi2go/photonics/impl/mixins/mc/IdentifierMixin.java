package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.game.minecraft.Id;
import net.minecraft.resources.Identifier;
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
