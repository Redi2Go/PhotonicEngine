package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.systems;

import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(IRenderSystem.class)
public interface IRenderSystemImpl {
    @Overwrite
    static IGpuDevice getDevice() {
        return (IGpuDevice) RenderSystem.getDevice();
    }
}
