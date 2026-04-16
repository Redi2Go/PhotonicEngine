package at.redi2go.photonics.impl.mixins.shaders.iris;

import at.redi2go.photonics.api.shaders.IPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IPackPath.class)
public interface IPathPackImpl {
    @Overwrite
    static IPackPath fromAbsolutePath(String absolutePath) {
        return (IPackPath) AbsolutePackPath.fromAbsolutePath(absolutePath);
    }
}
