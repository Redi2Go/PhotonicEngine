package at.redi2go.photonics.common.mixins.meshing;

import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ReloadableTexture.class)
public abstract class ReloadableTextureMixin {
    @ModifyConstant(method = "doLoad", constant = @Constant(intValue = 5))
    private int modifyUsage(int constant) {
        return constant | TextureUsage.COPY_SRC;
    }
}
