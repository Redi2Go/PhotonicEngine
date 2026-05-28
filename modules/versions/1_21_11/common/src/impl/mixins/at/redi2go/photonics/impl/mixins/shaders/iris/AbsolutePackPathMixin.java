package at.redi2go.photonics.impl.mixins.shaders.iris;

import at.redi2go.photonics.api.shaders.IPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.Optional;

@Mixin(AbsolutePackPath.class)
public abstract class AbsolutePackPathMixin implements IPackPath {
    @Shadow
    @Final
    private String path;

    @Shadow
    public abstract Optional<AbsolutePackPath> parent();

    @Shadow
    public abstract AbsolutePackPath resolve(String path);

    @Shadow
    public abstract Path resolved(Path root);

    @Override
    public Optional<IPackPath> ph$parent() {
        return (Optional) parent();
    }

    public IPackPath ph$resolve(String path) {
        return (IPackPath) resolve(path);
    }

    @Override
    public Path ph$resolved(Path root) {
        return resolved(root);
    }

    public boolean ph$startsWith(IPackPath path) {
        return this.path.startsWith(((AbsolutePackPath) path).getPathString());
    }

    public String ph$pathString() {
        return path;
    }
}
