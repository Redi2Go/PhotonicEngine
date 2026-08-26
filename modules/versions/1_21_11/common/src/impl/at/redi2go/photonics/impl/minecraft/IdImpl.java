package at.redi2go.photonics.impl.minecraft;

import at.redi2go.photonics.game.minecraft.Id;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Identifier.class)
public abstract class IdImpl implements Id {
    @Shadow @Final private String namespace;
    @Shadow @Final private String path;

    @Override
    public String ph$namespace() {
        return namespace;
    }

    @Override
    public String ph$path() {
        return path;
    }

    @Mixin(Id.class)
    public interface StaticMethods {
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

}
