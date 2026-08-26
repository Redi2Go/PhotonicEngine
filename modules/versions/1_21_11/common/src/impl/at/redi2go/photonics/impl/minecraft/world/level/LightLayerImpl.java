package at.redi2go.photonics.impl.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.world.level.ILightLayer;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LightLayer.class)
public abstract class LightLayerImpl implements ILightLayer {
    @Mixin(ILightLayer.class)
    public interface StaticMethods {
        @Overwrite
        static ILightLayer sky() {
            return (ILightLayer) (Object) LightLayer.SKY;
        }

        @Overwrite
        static ILightLayer block() {
            return (ILightLayer) (Object) LightLayer.BLOCK;
        }
    }
}
