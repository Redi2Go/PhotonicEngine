package at.redi2go.photonics.game.minecraft;

import at.redi2go.photonics.game.minecraft.world.level.ILevel;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3d;

public interface IMinecraft {
    /**
     * Schedules {@code runnable} to be executed on the render thread during the next frame.
     */
    static void schedule(Runnable runnable) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static @Nullable ILevel getLevel() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Vector3d getCameraPos() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static int getRenderDistance() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
