package at.redi2go.photonics.core.iris.patching;

import at.redi2go.photonics.api.shaders.IPackPath;
import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.core.Photonics;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class Patch {
    private static final int FORMAT_VERSION = 1;
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(IPackPath.class, new IPackPath.Adapter())
            .excludeFieldsWithoutExposeAnnotation()
            .setStrictness(Strictness.LENIENT)
            .create();

    private String name;

    @Expose private int formatVersion;
    @Expose private List<String> shaderPackNames;
    @Expose private List<String> supportedVersions;
    @Expose private boolean debug;
    @Expose private Set<IPackPath> alwaysPatched;

    private final Multimap<IPackPath, Path> patches =
            MultimapBuilder.hashKeys()
                    .arrayListValues()
                    .build();

    public static Optional<Patch> load(Path path, String patchName) {
        var patchJson = path.resolve("patch.json");
        if (Files.notExists(path)) {
            Photonics.LOGGER.error("patch.json for {} is missing!", patchName);
            return Optional.empty();
        }

        var patch = readPatch(patchJson, patchName);
        if (patch.isEmpty()) return Optional.empty();

        if (patch.get().formatVersion != FORMAT_VERSION) {
            Photonics.LOGGER.error("patch.json is using an of out date format version ({}, current {})", patch.get().formatVersion, FORMAT_VERSION);
            return Optional.empty();
        }

        if (patch.get().supportedVersions.contains(Photonics.getVersion().toString()))
            Photonics.LOGGER.warn(
                    "{} does not list the current photonics version ({}) as supported. Unexpected issues may occur!",
                    patchName,
                    Photonics.getVersion()
            );

        try(Stream<Path> patches = Files.walk(path)) {
            for (Iterator<Path> it = patches.iterator(); it.hasNext(); ) {
                Path pathFile = it.next();

                if (Files.isDirectory(pathFile)) continue;
                if (pathFile.equals(patchJson)) continue;

                Optional<IPackPath[]> files = readFileMacro(pathFile, patchName);
                if (files.isEmpty()) return Optional.empty();

                for (var file : files.get())
                    patch.get().patches.put(file, pathFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        patch.get().name = patchName;
        return patch;
    }

    public String name() {
        return name;
    }

    public boolean canBeApplied(IShaderPack shaderPack) {
        for (var name : shaderPackNames) {
            if (shaderPack.name().contains(name))
                return true;
        }

        return false;
    }

    public Collection<IPackPath> getFiles() {
        return patches.keySet();
    }

    public String applyPatches(
            IPackPath path,
            Function<IPackPath, @Nullable String> shaderSourceSupplier,
            boolean photonicsEnabled
    ) {
        String source = shaderSourceSupplier.apply(path);
        var patchFiles = patches.get(path);
        if (patchFiles.isEmpty()) return source;

        if (!photonicsEnabled && !alwaysPatched.contains(path)) return source;

        for (var patch : patchFiles) try {
            source = applySinglePatch(source, path, shaderSourceSupplier, patch);
        } catch (IOException e) {
            Photonics.LOGGER.warn("An exception was thrown applying patch file {}", patch, e);
        }

        return source;
    }

    private String applySinglePatch(
            @Nullable String source,
            IPackPath path,
            Function<IPackPath, String> shaderSourceSupplier,
            Path patchFile
    ) throws IOException {
        Queue<String> lines = new ArrayDeque<>();

        try(BufferedReader reader = Files.newBufferedReader(patchFile)) {
            while (true) {
                var line = reader.readLine();
                if (line == null) break;

                lines.add(line);
            }
        }

        IPackPath[] fileLocations = null;
        IPackPath[] templateLocations = null;

        List<Pair<String, String>> replacements = new ArrayList<>();

        boolean expectCommand = true;
        String searchString = null;
        StringBuilder codeBlockBuilder = new StringBuilder();

        while (!lines.isEmpty()) {
            String line = lines.poll();
            if (line.isBlank())
                continue;

            if (line.length() >= 2 && line.charAt(0) == '/' && line.charAt(1) == '/')
                continue;

            if (line.charAt(0) == '#') {
                int spaceIndex = line.indexOf(" ");
                if (spaceIndex == -1)
                    spaceIndex = line.length();

                String command = line.substring(1, spaceIndex);
                switch (command) {
                    case "file" -> fileLocations = readLocations(line.split(" "));

                    case "template" -> templateLocations = readLocations(line.split(" "));

                    case "create" -> {
                        StringBuilder createBuilder = new StringBuilder();
                        while (!lines.isEmpty())
                            createBuilder.append(lines.poll()).append('\n');

                        return createBuilder.toString();
                    }

                    case "replace" -> {
                        searchString = line.substring(spaceIndex + 2, line.length() - 1);
                        codeBlockBuilder = new StringBuilder();
                        expectCommand = false;
                    }
                    case "endreplace" -> {
                        if (searchString == null)
                            throw new PatchLoadException("#endreplace without proper #replace before");

                        replacements.add(Pair.of(searchString, codeBlockBuilder.toString()));
                        codeBlockBuilder = new StringBuilder();
                        expectCommand = true;
                    }

                    default -> { // normal preprocessor statement
                        codeBlockBuilder.append(line).append('\n');
                    }
                }
            } else if (expectCommand) {
                throw new PatchLoadException("Expected char '#' at position 0 in line " + line);
            } else {
                codeBlockBuilder.append(line).append('\n');
            }
        }

        Objects.requireNonNull(fileLocations);

        if (templateLocations == null) {
            templateLocations = fileLocations;
        } else if (templateLocations.length == 1) {
            IPackPath templateLocation = templateLocations[0];
            templateLocations = new IPackPath[fileLocations.length];

            Arrays.fill(templateLocations, templateLocation);
        } else if (templateLocations.length != fileLocations.length) {
            throw new PatchLoadException("The amount of template files must either be 1, or equal to the amount of 'file' locations");
        }

        var index = List.of(fileLocations).indexOf(path);

        IPackPath templateLocation = templateLocations[index];
        if (!templateLocation.equals(path))
            source = shaderSourceSupplier.apply(templateLocation);

        if (source == null) return "";

        for (Pair<String, String> replacement : replacements)
            source = source.replace(replacement.getLeft(), replacement.getRight());

        return source;
    }

    private static Optional<Patch> readPatch(Path patchJson, String patchName) {
        try {
            return Optional.of(
                    GSON.fromJson(
                            Files.newBufferedReader(patchJson),
                            Patch.class
                    )
            );
        } catch (IOException e) {
        } catch (Exception e) {
            Photonics.LOGGER.error("Could not parse patch.json for {}", patchName, e);
            return Optional.empty();
        }
    }

    private static Optional<IPackPath[]> readFileMacro(Path path, String patchName) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            var line = reader.readLine();
            if (!line.startsWith("#file")) {
                Photonics.LOGGER.error("{} of {} is missing #file at the top of the file", path, patchName);
                return Optional.empty();
            }

            return Optional.of(readLocations(line.split(" ")));
        } catch (IOException e) {
            Photonics.LOGGER.error("An exception was thrown reading {} of {}", path, patchName, e);
            return Optional.empty();
        }
    }

    private static IPackPath[] readLocations(String[] lineTokens) throws PatchLoadException {
        if (lineTokens.length == 1)
            throw new PatchLoadException("You must provide location directives");

        IPackPath[] locations = new IPackPath[lineTokens.length - 1];
        for (int i = 1; i < lineTokens.length; i++) {
            String location = lineTokens[i].substring(1, lineTokens[i].length() - 1);
            if (!location.startsWith("/"))
                throw new PatchLoadException("Location file must start with a '/'");

            locations[i - 1] = IPackPath.fromAbsolutePath(location);
        }

        return locations;
    }
}
