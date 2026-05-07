package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InternalTextureFormat.class)
@Implements(@Interface(iface = ITextureFormat.class, prefix = "ph$"))
public abstract class InternalTextureFormatMixin implements ITextureFormat {
}
