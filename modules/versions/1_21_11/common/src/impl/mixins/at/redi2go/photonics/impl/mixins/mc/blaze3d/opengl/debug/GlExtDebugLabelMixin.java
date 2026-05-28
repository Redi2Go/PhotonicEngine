package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.debug;


import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlDebugLabelExt;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import net.minecraft.util.StringUtil;
import org.lwjgl.opengl.EXTDebugLabel;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import static org.lwjgl.opengl.GL11.GL_TEXTURE;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Ext")
@Implements(@Interface(iface = GlDebugLabelExt.class, prefix = "ph$"))
public abstract class GlExtDebugLabelMixin {
    public void ph$applyLabel(IGlTexture texture) {
        EXTDebugLabel.glLabelObjectEXT(GL_TEXTURE, texture.handle(), StringUtil.truncateStringIfNecessary(texture.ph$label(), 256, true));
    }
}