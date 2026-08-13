package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.core.iris.IrisPackPath;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IrisPackPath.class)
public interface IPathPackImpl {
    @Overwrite
    static IrisPackPath fromAbsolutePath(String absolutePath) {
        return (IrisPackPath) AbsolutePackPath.fromAbsolutePath(absolutePath);
    }
}
