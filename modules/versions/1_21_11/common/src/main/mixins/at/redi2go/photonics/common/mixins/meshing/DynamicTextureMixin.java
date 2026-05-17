package at.redi2go.photonics.common.mixins.meshing;

import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DynamicTexture.class)
public abstract class DynamicTextureMixin {
    @ModifyConstant(method = "createTexture(Ljava/lang/String;)V", constant = @Constant(intValue = 5))
    private int modifyUsage1(int constant) {
        return constant | TextureUsage.COPY_SRC;
    }

    @ModifyConstant(method = "createTexture(Ljava/util/function/Supplier;)V", constant = @Constant(intValue = 5))
    private int modifyUsage2(int constant) {
        return constant | TextureUsage.COPY_SRC;
    }
}
