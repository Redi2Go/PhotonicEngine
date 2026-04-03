package at.redi2go.photonics.core.iris.patching.sources;

import at.redi2go.photonics.core.iris.patching.PatchSource;

import java.nio.file.Path;
import java.util.stream.Stream;

public class JarSource implements PatchSource {
    public static final JarSource INSTANCE = new JarSource();

    private JarSource() {}

    @Override
    public Stream<Path> streamPatches() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void onChanged(Runnable listener) {
        throw new UnsupportedOperationException("TODO");
    }
}
