package at.redi2go.photonics.core.iris.patching;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface PatchSource {
    Stream<Path> streamPatches();
}
