package at.redi2go.photonics.impl.mixins.mc.blaze3d.buffers;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import com.mojang.blaze3d.buffers.GpuBuffer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GpuBuffer.MappedView.class)
public interface GpuBufferMappedViewMixin extends IGpuBuffer.MappedView {
}
