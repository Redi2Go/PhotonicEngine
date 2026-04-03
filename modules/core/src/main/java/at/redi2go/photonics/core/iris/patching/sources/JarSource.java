package at.redi2go.photonics.core.iris.patching.sources;

import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.patching.PatchSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class JarSource implements PatchSource {
    public static final JarSource INSTANCE = new JarSource();

    private JarSource() {}

    @Override
    public Stream<Path> streamPatches() {
        Path includedPatches = Photonics.getAssetsPath()
                .resolve("photonics")
                .resolve("patches");

        try {
            return Files.list(includedPatches);
        } catch (IOException e) {
           Photonics.LOGGER.warn("An exception was thrown listing Photonics's included patches", e);
           return Stream.empty();
        }
    }

    @Override
    public void onChanged(Runnable listener) {
        //TODO
    }
}
