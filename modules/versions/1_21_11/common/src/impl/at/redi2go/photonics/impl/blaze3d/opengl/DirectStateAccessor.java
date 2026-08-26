package at.redi2go.photonics.impl.blaze3d.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public interface DirectStateAccessor {
    @Invoker
    void invokeBindFrameBufferTextures(int fbo, int color0, int depth, int mipLevel, int target);
}
