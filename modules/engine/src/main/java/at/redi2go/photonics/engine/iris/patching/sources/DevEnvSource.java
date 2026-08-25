package at.redi2go.photonics.engine.iris.patching.sources;

import at.redi2go.photonics.engine.iris.patching.PatchSource;

import java.nio.file.Path;
import java.util.stream.Stream;

public class DevEnvSource implements PatchSource {
    @Override
    public Stream<Path> streamPatches() {
        //TODO
        return Stream.empty();
    }

    @Override
    public void onChanged(Runnable listener) {
        //TODO
    }
}
