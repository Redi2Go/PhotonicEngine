package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.debug;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Empty")
@Implements(@Interface(iface = GlDebugLabelExt.class, prefix = "ph$"))
public abstract class GlEmptyDebugLabelMixin {
    public void ph$applyLabel(IGlTexture texture) {

    }
}
