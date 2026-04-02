package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Minecraft.class, remap = false)
public interface IMinecraftImpl {
    @Overwrite
    static void schedule(Runnable runnable) {
        net.minecraft.client.Minecraft.getInstance()
                .execute(runnable);
    }
}
