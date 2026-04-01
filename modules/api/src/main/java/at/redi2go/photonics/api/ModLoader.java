package at.redi2go.photonics.api;

import java.nio.file.Path;

public interface ModLoader {
    static Path getGameDir() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }

    static Path getConfigDir() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
