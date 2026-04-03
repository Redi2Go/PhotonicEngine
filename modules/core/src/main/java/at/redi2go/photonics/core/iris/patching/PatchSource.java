package at.redi2go.photonics.core.iris.patching;

import at.redi2go.photonics.api.Disposable;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface PatchSource {
    Stream<Path> streamPatches();

    void onChanged(Runnable listener);
}
