package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Id.class)
public interface IdImpl {
    @Overwrite
    static Id fromNamespaceAndPath(String namespace, String path) {
        return (Id) (Object) Identifier.fromNamespaceAndPath(namespace, path);
    }

    @Overwrite
    static Id parse(String string) {
        return (Id) (Object) Identifier.parse(string);
    }

    @Overwrite
    static Id withDefaultNamespace(String path) {
        return (Id) (Object) Identifier.withDefaultNamespace(path);
    }
}
