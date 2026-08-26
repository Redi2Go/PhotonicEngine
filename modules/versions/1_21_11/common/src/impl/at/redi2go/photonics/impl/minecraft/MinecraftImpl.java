package at.redi2go.photonics.impl.minecraft;

import at.redi2go.photonics.game.minecraft.IMinecraft;
import at.redi2go.photonics.game.minecraft.client.player.ILocalPlayer;
import at.redi2go.photonics.game.minecraft.world.level.ILevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IMinecraft.class)
public interface MinecraftImpl {
    @Overwrite
    static void schedule(Runnable runnable) {
        Minecraft.getInstance()
                .execute(runnable);
    }

    @Overwrite
    static @Nullable ILevel getLevel() {
        return (ILevel) Minecraft.getInstance()
                .level;
    }

    @Overwrite
    static @Nullable ILocalPlayer getLocalPlayer() {
        return (ILocalPlayer) Minecraft.getInstance().player;
    }

    @Overwrite
    static Vector3d getCameraPos() {
        Vec3 position = Minecraft.getInstance()
                .gameRenderer
                .getMainCamera()
                .position();

        return new Vector3d(position.x, position.y, position.z);
    }

    @Overwrite
    static int getRenderDistance() {
        return Minecraft.getInstance()
                .options
                .getEffectiveRenderDistance();
    }
}
