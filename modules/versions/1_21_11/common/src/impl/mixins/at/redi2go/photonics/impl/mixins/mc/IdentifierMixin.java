package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Identifier.class)
public abstract class IdentifierMixin implements Id {
    @Shadow
    @Final
    private String namespace;

    @Shadow
    @Final
    private String path;

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public String path() {
        return path;
    }
}
