package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.buffer;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import java.nio.ByteBuffer;

@Mixin(GlBuffer.GlMappedView.class)
@Implements(@Interface(iface = IGpuBuffer.MappedView.class, prefix = "ph$"))
public abstract class GlMappedViewMixin implements IGpuBuffer.MappedView {

}
