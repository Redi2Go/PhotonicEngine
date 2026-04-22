package at.redi2go.photonics.impl.mixins.mc;

import at.redi2go.photonics.api.mc.Minecraft;
import at.redi2go.photonics.api.mc.world.level.ILevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = Minecraft.class, remap = false)
public interface IMinecraftImpl {
    @Overwrite
    static void schedule(Runnable runnable) {
        net.minecraft.client.Minecraft.getInstance()
                .execute(runnable);
    }

    @Overwrite
    static @Nullable ILevel getLevel() {
        return (ILevel) net.minecraft.client.Minecraft.getInstance()
                .level;
    }

    @Overwrite
    static Vector3d getCameraPos() {
        Vec3 position = net.minecraft.client.Minecraft.getInstance()
                .gameRenderer
                .getMainCamera()
                .position();

        return new Vector3d(position.x, position.y, position.z);
    }
}
