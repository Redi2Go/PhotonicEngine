package at.redi2go.photonics.impl.minecraft;

import at.redi2go.photonics.game.minecraft.IIdentifier;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Identifier.class)
public abstract class IdentifierImpl implements IIdentifier {
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

    @Mixin(IIdentifier.class)
    public interface StaticMethods {
        @Overwrite
        static IIdentifier fromNamespaceAndPath(String namespace, String path) {
            return (IIdentifier) (Object) Identifier.fromNamespaceAndPath(namespace, path);
        }

        @Overwrite
        static IIdentifier parse(String string) {
            return (IIdentifier) (Object) Identifier.parse(string);
        }

        @Overwrite
        static IIdentifier withDefaultNamespace(String path) {
            return (IIdentifier) (Object) Identifier.withDefaultNamespace(path);
        }
    }

}
