package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.core.iris.IrisPack;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(IrisPack.class)
public interface IShaderPackImpl {
    @Overwrite
    static Optional<IrisPack> getCurrentPack() {
        return Iris.getCurrentPack().map(e -> (IrisPack) e);
    }
}
