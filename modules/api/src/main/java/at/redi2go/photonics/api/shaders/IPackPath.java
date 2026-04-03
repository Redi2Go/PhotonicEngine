package at.redi2go.photonics.api.shaders;

import java.nio.file.Path;
import java.util.Optional;

public interface IPackPath {
    Optional<IPackPath> parent();

    IPackPath resolve(String path);

    Path resolved(Path root);

    boolean startsWith(IPackPath path);

    default boolean startsWith(String absolutePath) {
        return startsWith(fromAbsolutePath(absolutePath));
    }

    static IPackPath fromAbsolutePath(String absolutePath) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
