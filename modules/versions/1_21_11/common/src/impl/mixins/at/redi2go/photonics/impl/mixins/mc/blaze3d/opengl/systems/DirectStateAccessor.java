package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DirectStateAccess.class)
public interface DirectStateAccessor {
    @Invoker
    void invokeBindFrameBufferTextures(int i, int j, int k, int l, int m);
}
