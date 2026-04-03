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
 * The extension for Iris (maybe aperture?), handles world building, light list, and patching.
 */
public abstract class PhotonicsExtension implements AutoCloseable {
    // TODO replace with comment at the top of the file
    private static final Set<String> AUTO_REPLACED_FILES = Set.of("photonics.glsl", "ph_samplers.glsl");

    protected final IShaderPack pack;
    private final @Nullable Patch patch;

    public PhotonicsExtension(IShaderPack pack, @Nullable Patch patch) {
        this.pack = pack;
        this.patch = patch;
    }

    public List<IPackPath> getCreatedFiles() {
        List<IPackPath> createdFiles = new ArrayList<>();

        if (patch != null)
            createdFiles.addAll(patch.getFiles());

        Path includedShaders = Photonics.getAssetsPath()
                .resolve("photonics")
                .resolve("shaders")
                .resolve("photonics");

        try (Stream<Path> shaders = Files.walk(includedShaders)) {
            shaders.forEach(file -> {
                if (Files.isDirectory(file)) return;

                var relativePath = includedShaders.relativize(file);
                createdFiles.add(
                        IPackPath.fromAbsolutePath("/photonics")
                                .resolve(relativePath.toString().replace('\\', '/'))
                );
            });
        } catch (IOException e) {
            Photonics.LOGGER.error("An error occurred loading Photonics", e);
        }

        return createdFiles;
    }

    public @Nullable String readPhotonicsFile(
            IPackPath packPath,
            Function<IPackPath, @Nullable String> shaderSourceSupplier
    ) {
        Path includedShaders = Photonics.getAssetsPath()
                .resolve("photonics")
                .resolve("shaders");

        Path realPath = packPath.resolved(includedShaders);
        Path relativePath = includedShaders.resolve("photonics")
                .relativize(realPath);

        @Nullable String source = null;

        readFile:
        {
            // If no patch is applied check if the shader has a replacement
            if (patch == null && !AUTO_REPLACED_FILES.contains(relativePath.toString())) {
                source = shaderSourceSupplier.apply(packPath);
                if (source != null) break readFile;
            }

            // Try loading shader from assets in jar
            if (Files.exists(realPath)) {
                source = Fs.tryReadString(realPath).orElse(null);
                if (source != null) break readFile;
            }
        }

        if (patch == null) return source;
        @Nullable String loadedSource = source;

        source = patch.applyPatches(
                packPath,
                p -> {
                    if (p.equals(packPath))
                        return loadedSource;

                    return shaderSourceSupplier.apply(p);
                },
                pack.properties().isPhotonicsEnabled()
        );

        return source;
    }

    /**
     * Reads a potentially patched shader file, also responsible for loading Photonics' built in shader files.
     */
    public @Nullable String readShaderFile(
            IPackPath path,
            Function<IPackPath, @Nullable String> shaderSourceSupplier
    ) {
        if (path.startsWith("/photonics")) return readPhotonicsFile(path, shaderSourceSupplier);
        if (patch == null) return shaderSourceSupplier.apply(path);

        return patch.applyPatches(
                path,
                shaderSourceSupplier,
                pack.properties().isPhotonicsEnabled()
        );
    }

    /**
     * Creates the PhotonicsExtension for a given {@code shaderPack}
     */
    static PhotonicsExtension create(IShaderPack shaderPack) {
        Patch patch = null;

        if (!shaderPack.supportsPhotonics())
            patch = getPatchList().loadPatch(shaderPack).orElse(null);

        return shaderPack.properties().isPhotonicsEnabled() ?
                new Raytracer(shaderPack, patch) :
                new DisabledExt(shaderPack, patch);
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
