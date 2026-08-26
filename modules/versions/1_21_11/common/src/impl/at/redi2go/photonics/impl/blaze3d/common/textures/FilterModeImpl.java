package at.redi2go.photonics.impl.blaze3d.common.textures;

import at.redi2go.photonics.game.blaze3d.textures.IFilterMode;
import com.mojang.blaze3d.textures.FilterMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FilterMode.class)
public abstract class FilterModeImpl implements IFilterMode {
    @Mixin(IFilterMode.class)
    public interface StaticMethods {
        @Overwrite
        static IFilterMode nearest() {
            return (IFilterMode) (Object) FilterMode.NEAREST;
        }

        @Overwrite
        static IFilterMode linear() {
            return (IFilterMode) (Object) FilterMode.LINEAR;
        }
    }
}
