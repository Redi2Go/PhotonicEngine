package at.redi2go.photonics.impl.mixins.shaders.iris;

import at.redi2go.photonics.api.shaders.IPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbsolutePackPath.class)
public abstract class AbsolutePackPathMixin implements IPackPath {
    @Shadow @Final private String path;

    @Shadow public abstract AbsolutePackPath shadow$resolve(String path);

    @Override
    public IPackPath resolve(String path) {
        return (IPackPath) shadow$resolve(path);
    }

    @Override
    public boolean startsWith(IPackPath path) {
        return this.path.startsWith(((AbsolutePackPath) path).getPathString());
    }

    @Override
    public String pathString() {
        return path;
    }
}
