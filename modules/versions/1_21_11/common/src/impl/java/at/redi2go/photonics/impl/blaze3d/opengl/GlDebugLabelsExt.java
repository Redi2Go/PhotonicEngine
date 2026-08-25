package at.redi2go.photonics.impl.blaze3d.opengl;

import at.redi2go.photonics.impl.blaze3d.opengl.systems.GlDeviceImpl;
import at.redi2go.photonics.impl.blaze3d.opengl.textures.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.StringUtil;
import org.lwjgl.opengl.EXTDebugLabel;
import org.lwjgl.opengl.KHRDebug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

public interface GlDebugLabelsExt {
    void applyLabel(GlTexture texture);

    @Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Core")
    abstract class CoreImpl implements GlDebugLabelsExt {
        @Shadow @Final private int maxLabelLength;

        @Override
        public void applyLabel(GlTexture texture) {
            KHRDebug.glObjectLabel(5890, texture.ph$id(), StringUtil.truncateStringIfNecessary(texture.ph$getLabel(), this.maxLabelLength, true));
        }
    }

    @Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Empty")
    abstract class EmptyImpl implements GlDebugLabelsExt {
        @Override
        public void applyLabel(GlTexture texture) {

        }
    }

    @Mixin(targets = "com.mojang.blaze3d.opengl.GlDebugLabel$Ext")
    abstract class ExtImpl implements GlDebugLabelsExt {
        @Override
        public void applyLabel(GlTexture texture) {
            EXTDebugLabel.glLabelObjectEXT(5890, texture.ph$id(), StringUtil.truncateStringIfNecessary(texture.ph$getLabel(), 256, true));
        }
    }
}
