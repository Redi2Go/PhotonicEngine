package at.redi2go.photonics.impl.mixins.shaders.iris;

import at.redi2go.photonics.api.shaders.IShaderPack;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(IShaderPack.class)
public interface IShaderPackImpl {
    @Overwrite
    static Optional<IShaderPack> getCurrentPack() {
        return Iris.getCurrentPack().map(e -> (IShaderPack) e);
    }
}
