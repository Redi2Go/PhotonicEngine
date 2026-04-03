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
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The extension for Iris (maybe aperture?), handles world building, light list, and patching.
 */
public abstract class PhotonicsExtension implements AutoCloseable {
    private final @Nullable Patch patch;

    public PhotonicsExtension(@Nullable Patch patch) {
        this.patch = patch;
    }

    /**
     * Reads a potentially patched shader file, also responsible for loading Photonics' built in shader files.
     */
    public String readShaderFile(
            IPackPath path,
            Function<IPackPath, @Nullable String> shaderSourceSupplier
    ) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Creates the PhotonicsExtension for a given {@code shaderPack}
     */
    static PhotonicsExtension create(IShaderPack shaderPack) {
        throw new UnsupportedOperationException("TODO");
    }






    // Patch tracking
    private static final List<PatchSource> SOURCES;

    private static PatchList patchList = null;
    private static boolean needsReload = true;

    static {
        var sourcesBuilder = ImmutableList.<PatchSource>builder();

        sourcesBuilder.add(JarSource.INSTANCE);
        sourcesBuilder.add(ShaderPatchesSource.INSTANCE);

        if (Photonics.isDevEnvironment()) sourcesBuilder.add(new DevEnvSource());

        SOURCES = sourcesBuilder.build();

        // Reload is deferred so in use patches are not discarded
        for (var source : SOURCES)
            source.onChanged(() -> needsReload = true);
    }

    private static synchronized void checkForReload() {
        if (!needsReload) return;

        // Done in a weird order in case PatchList init throws an exception
        // If you were to first close the old list, it could be left with stale patch paths
        var oldList = patchList;

        patchList = new PatchList(SOURCES);
        needsReload = false;

        if (oldList != null) oldList.close();
    }

    public static PatchList getPatchList() {
        if (needsReload) checkForReload();

        return Objects.requireNonNull(patchList);
    }
}
