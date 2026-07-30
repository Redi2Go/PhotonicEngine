package at.redi2go.photonics.client.rendering.patching;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.ShaderPackPath;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class Patch {
   private static final int FORMAT_VERSION = 1;
   private static final Set<String> REPORTED_MISSING_ANCHORS = ConcurrentHashMap.newKeySet();
   private final List<String> shaderPackNames;
   private final Map<String, Function<Path, String>> patchedFilesSuppliers;
   private final boolean debug;
   public final List<String> files;
   private final boolean photonicsEnabled;

   private Patch(
      List<String> shaderPackNames, Map<String, Function<Path, String>> patchedFilesSuppliers, List<String> files, boolean debug, boolean photonicsEnabled
   ) {
      this.shaderPackNames = shaderPackNames;
      this.patchedFilesSuppliers = patchedFilesSuppliers;
      this.files = files;
      this.debug = debug;
      this.photonicsEnabled = photonicsEnabled;
   }

   public static Patch of(Path patchPath, boolean photonicsEnabled) throws IOException {
      Patch.PatchInfo info = (Patch.PatchInfo)new Gson().fromJson(readPath(patchPath.resolve("patch.json")), Patch.PatchInfo.class);
      if (info == null) {
         throw new Patch.PatchLoadException("Could not parse patch.json");
      }

      if (info.formatVersion != 1) {
         throw new Patch.PatchLoadException("The patch is written in an unsupported format");
      }

      if (!info.supportedVersions.contains(Photonics.MOD_VERSION)) {
         Photonics.warn("Patch " + patchPath + " is probably not compatible");
      }

      Map<String, Function<Path, String>> patchFiles = new HashMap<>();
      List<String> files = new ArrayList<>();

      try (Stream<Path> patches = Files.walk(patchPath)) {
         Iterator<Path> it = patches.iterator();

         while (it.hasNext()) {
            Path patchFile = it.next();
            if (!Files.isDirectory(patchFile) && !patchFile.getFileName().toString().equals("patch.json")) {
               readPatchFile(patchFiles, files, patchFile);
            }
         }
      }

      if (!photonicsEnabled) {
         for (String file : patchFiles.keySet().toArray(new String[0])) {
            String fileWithoutShaders = file.substring("shaders".length());
            if (!info.alwaysPatched.contains(fileWithoutShaders)) {
               patchFiles.remove(file);
            }
         }

         files.removeIf(filex -> !info.alwaysPatched.contains(filex.substring("/shaders".length())));
      }

      return new Patch(info.shaderPackNames, patchFiles, files, info.debug, photonicsEnabled);
   }

   private static void readPatchFile(Map<String, Function<Path, String>> patchedFilesSuppliers, List<String> files, Path patchFile) throws IOException {
      Queue<String> lines = new LinkedList<>(Files.readAllLines(patchFile));
      String[] fileLocations = null;
      String[] templateLocations = null;
      String createContent = null;
      List<Pair<String, String>> replacements = new ArrayList<>();
      boolean expectCommand = true;
      String searchString = null;
      StringBuilder codeBlockBuilder = new StringBuilder();

      while (!lines.isEmpty()) {
         String line = lines.poll();
         if (!line.isBlank() && (line.length() < 2 || line.charAt(0) != '/' || line.charAt(1) != '/')) {
            if (line.charAt(0) == '#') {
               int spaceIndex = line.indexOf(" ");
               if (spaceIndex == -1) {
                  spaceIndex = line.length();
               }

               String command = line.substring(1, spaceIndex);
               switch (command) {
                  case "file":
                     fileLocations = readLocations(line.split(" "));
                     break;
                  case "template":
                     templateLocations = readLocations(line.split(" "));
                     break;
                  case "create":
                     StringBuilder createBuilder = new StringBuilder();

                     while (!lines.isEmpty()) {
                        createBuilder.append(lines.poll()).append('\n');
                     }

                     createContent = createBuilder.toString();
                     break;
                  case "replace":
                     searchString = line.substring(spaceIndex + 2, line.length() - 1);
                     codeBlockBuilder = new StringBuilder();
                     expectCommand = false;
                     break;
                  case "endreplace":
                     if (searchString == null) {
                        throw new Patch.PatchLoadException("#endreplace without proper #replace before");
                     }

                     replacements.add(Pair.of(searchString, codeBlockBuilder.toString()));
                     codeBlockBuilder = new StringBuilder();
                     expectCommand = true;
                     break;
                  default:
                     codeBlockBuilder.append(line).append('\n');
               }
            } else {
               if (expectCommand) {
                  throw new Patch.PatchLoadException("Expected char '#' at position 0 in line " + line);
               }

               codeBlockBuilder.append(line).append('\n');
            }
         }
      }

      if (fileLocations == null) {
         throw new Patch.PatchLoadException("You must specify the file locations using 'file'");
      }

      if (templateLocations == null) {
         templateLocations = fileLocations;
      } else if (templateLocations.length == 1) {
         String templateLocation = templateLocations[0];
         templateLocations = new String[fileLocations.length];
         Arrays.fill(templateLocations, templateLocation);
      } else if (templateLocations.length != fileLocations.length) {
         throw new Patch.PatchLoadException("The amount of template files must eiter be 1, or equal to the amount of 'file' locations");
      }

      files.addAll(Arrays.asList(fileLocations));
      String createContentFinal = createContent;

      for (int i = 0; i < fileLocations.length; i++) {
         String fileLocation = fileLocations[i].substring(1);
         String templateLocation = templateLocations[i].substring(1);
         String patchDescription = patchFile.getFileName() + " -> " + fileLocation;
         patchedFilesSuppliers.put(fileLocation, rootPath -> {
            if (createContentFinal != null) {
               return createContentFinal;
            }

            String source = Raytracer.readShaderFile(new ShaderPackPath(rootPath.resolve(templateLocation)), false);
            if (source == null) {
               return null;
            }

            for (Pair<String, String> replacement : replacements) {
               source = replaceOrWarn(source, replacement.getLeft(), replacement.getRight(), patchDescription);
            }

            return source;
         });
      }
   }

   static String replaceOrWarn(String source, String search, String replacement, String context) {
      return replaceOrWarn(source, search, replacement, context, true);
   }

   static String replaceOrWarn(String source, String search, String replacement, String context, boolean emitWarning) {
      if (!source.contains(search)) {
         String reportKey = context + '\u0000' + search;
         if (emitWarning && REPORTED_MISSING_ANCHORS.add(reportKey)) {
            String compactAnchor = search.replaceAll("\\s+", " ").trim();
            if (compactAnchor.length() > 120) {
               compactAnchor = compactAnchor.substring(0, 117) + "...";
            }

            Photonics.warn("Shader patch anchor missing [{}]: {}", context, compactAnchor);
         }

         return source;
      }

      return source.replace(search, replacement);
   }

   private static String[] readLocations(String[] lineTokens) throws Patch.PatchLoadException {
      if (lineTokens.length == 1) {
         throw new Patch.PatchLoadException("You must provide location directives");
      }

      String[] locations = new String[lineTokens.length - 1];

      for (int i = 1; i < lineTokens.length; i++) {
         String location = lineTokens[i].substring(1, lineTokens[i].length() - 1);
         if (!location.startsWith("/")) {
            throw new Patch.PatchLoadException("Location file must start with a '/'");
         }

         locations[i - 1] = "/shaders" + location;
      }

      return locations;
   }

   public String readPatchedFile(ShaderPackPath path) {
      Function<Path, String> patchedFileSupplier = this.patchedFilesSuppliers.get(path.getRelativePath());
      if (patchedFileSupplier == null) {
         return null;
      }

      String patchedSource = patchedFileSupplier.apply(path.getRootPath());
      if (this.debug && patchedSource != null) {
         try {
            Path debugFolder = Path.of("debug");
            Path debugFilePath = debugFolder.resolve(path.getRelativePath());
            Files.createDirectories(debugFilePath.getParent());
            if (!Files.exists(debugFilePath)) {
               Files.createFile(debugFilePath);
            }

            Files.writeString(debugFilePath, patchedSource);
         } catch (IOException e) {
            Photonics.error(e);
         }
      }

      return patchedSource;
   }

   public boolean canBeApplied(String shaderPackName, boolean photonicsEnabled) {
      return this.photonicsEnabled == photonicsEnabled && this.shaderPackNames.stream().anyMatch(shaderPackName::contains);
   }

   private static String readPath(Path path) throws IOException {
      return Files.readString(path);
   }

   private record PatchInfo(int formatVersion, List<String> shaderPackNames, List<String> supportedVersions, boolean debug, Set<String> alwaysPatched) {
   }

   public static class PatchLoadException extends IOException {
      public PatchLoadException(String message) {
         super(message);
      }
   }
}
