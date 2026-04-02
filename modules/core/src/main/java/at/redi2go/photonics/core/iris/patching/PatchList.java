package at.redi2go.photonics.core.iris.patching;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.Photonics;
import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class PatchList implements Disposable {
    private final List<FileSystem> fileSystems;
    private final List<Patch> patches;

    public PatchList(List<PatchSource> sources) {
        fileSystems = new ArrayList<>();
        patches = new ArrayList<>();

        try (
                var patches = sources.stream()
                        .flatMap(PatchSource::streamPatches)
                        .filter(Files::exists)
                        .map(p -> {
                            var name = p.getFileName().toString();

                            var extensionIndex = name.lastIndexOf('.');
                            if (extensionIndex == -1) return Triple.<Path, String, FileSystem>of(p, name, null);

                            var extension = name.substring(extensionIndex);
                            if (!extension.equals(".zip")) return Triple.<Path, String, FileSystem>of(p, name, null);

                            try {
                                final var fs = FileSystems.newFileSystem(p);
                                return Triple.of(p, name, fs);
                            } catch(IOException e) {
                                Photonics.LOGGER.error("An exception was thrown creating file system for {}", name, e);

                                return Triple.<Path, String, FileSystem>of(p, name, null);
                            }
                        })
        ) {
            patches.forEach(e -> {
                if (e.getRight() != null) fileSystems.add(e.getRight());

                Optional<Patch> patch = Patch.load(e.getLeft(), e.getMiddle());
                if (patch.isEmpty()) {
                    Photonics.LOGGER.error("There was an issue loading {}, it will be disabled until reload", e.getMiddle());
                    return;
                }

                this.patches.add(patch.get());
            });
        }
    }

    @Override
    public void close() {
        for (var fs : fileSystems) {
            try {
                fs.close();
            } catch(IOException e) {
                Photonics.LOGGER.error("An exception was thrown while closing a patch file system");
            }
        }
    }
}
