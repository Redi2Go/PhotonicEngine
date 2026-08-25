package at.redi2go.photonics.impl.blaze3d;

import at.redi2go.photonics.game.blaze3d.systems.IGpuDevice;
import at.redi2go.photonics.game.blaze3d.systems.IRenderSystem;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.ByteBuffer;

@Mixin(IRenderSystem.class)
public interface RenderSystemImpl {
    @Overwrite
    static IGpuDevice getDevice() {
        return (IGpuDevice) RenderSystem.getDevice();
    }
}
