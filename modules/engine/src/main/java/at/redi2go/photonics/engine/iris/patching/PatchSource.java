package at.redi2go.photonics.engine.iris.patching;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface PatchSource {
    Stream<Path> streamPatches();

    void onChanged(Runnable listener);
}
