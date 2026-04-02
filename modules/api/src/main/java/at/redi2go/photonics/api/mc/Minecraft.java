package at.redi2go.photonics.api.mc;

import at.redi2go.photonics.api.mc.world.level.ILevel;
import org.jetbrains.annotations.Nullable;

public interface Minecraft {
    static void schedule(Runnable runnable) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static @Nullable ILevel getLevel() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
