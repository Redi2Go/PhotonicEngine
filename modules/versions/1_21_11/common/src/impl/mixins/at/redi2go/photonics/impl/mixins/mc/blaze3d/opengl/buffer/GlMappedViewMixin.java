package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.buffer;

import at.redi2go.photonics.game.gpu.buffers.IGpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(GlBuffer.GlMappedView.class)
public abstract class GlMappedViewMixin implements IGpuBuffer.MappedView {
    @Shadow
    public abstract ByteBuffer data();

    @Override
    public ByteBuffer ph$data() {
        return data();
    }
}
