package at.redi2go.photonics.engine.iris.patching;

import java.io.IOException;

public class PatchLoadException extends IOException {
    public PatchLoadException(String message) {
        super(message);
    }
}
