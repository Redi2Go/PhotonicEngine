package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.shaders.IPackPath;
import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.patching.Patch;
import at.redi2go.photonics.core.iris.patching.PatchList;
import at.redi2go.photonics.core.iris.patching.PatchSource;
import at.redi2go.photonics.core.iris.patching.sources.DevEnvSource;
import at.redi2go.photonics.core.iris.patching.sources.JarSource;
import at.redi2go.photonics.core.iris.patching.sources.ShaderPatchesSource;
import at.redi2go.photonics.core.util.Fs;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The extension for Iris (maybe aperture?), handles world building, light list, ect.
 */
public interface PhotonicsExtension extends AutoCloseable {
    class Disabled implements PhotonicsExtension {
        @Override
        public void close() throws Exception {

        }
    }
}