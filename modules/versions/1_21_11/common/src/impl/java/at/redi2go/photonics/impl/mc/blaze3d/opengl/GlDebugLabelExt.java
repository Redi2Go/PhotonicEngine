package at.redi2go.photonics.impl.mc.blaze3d.opengl;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.GlTexture2D;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;

public interface GlDebugLabelExt {
    void applyLabel(IGlTexture texture2D);

    static GlDebugLabelExt getInstance() {
        return (GlDebugLabelExt) ((GlDevice) RenderSystem.getDevice()).debugLabels();
    }
}
