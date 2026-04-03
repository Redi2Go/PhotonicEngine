package at.redi2go.photonics.core.iris.patching.sources;

import at.redi2go.photonics.core.iris.patching.PatchSource;

import java.nio.file.Path;
import java.util.stream.Stream;

public class ShaderPatchesSource implements PatchSource {
    public static final ShaderPatchesSource INSTANCE = new ShaderPatchesSource();

    private ShaderPatchesSource() {}

    @Override
    public Stream<Path> streamPatches() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void onChanged(Runnable listener) {
        throw new UnsupportedOperationException("TODO");
    }
}
