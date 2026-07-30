package at.redi2go.photonics.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ShaderPackPath {
   private final Path path;
   private final Path rootPath;
   private final String relativePath;

   public ShaderPackPath(Path path) {
      path = path.toAbsolutePath();
      this.path = path;
      Path shadersFolder = null;
      Path tempPath = path.getFileSystem().getPath("/");

      for (Path component : path) {
         if ("shaders".equals(Objects.toString(tempPath.getFileName()))) {
            shadersFolder = tempPath;
         }

         tempPath = tempPath.resolve(component);
      }

      if (shadersFolder == null) {
         throw new IllegalStateException("File does not describe a shader-file");
      }

      this.rootPath = shadersFolder.getParent().toAbsolutePath();
      this.relativePath = this.rootPath.relativize(path).toString().replace('\\', '/');
   }

   public boolean isPhotonicsPath() {
      return this.relativePath.startsWith("shaders/photonics/");
   }

   public String getRelativeToPhotonics() {
      return this.relativePath.substring("shaders/photonics/".length());
   }

   public boolean exists() {
      return Files.exists(this.path);
   }

   public String readFile() throws IOException {
      return Files.readString(this.path);
   }

   public Path getRootPath() {
      return this.rootPath;
   }

   public String getRelativePath() {
      return this.relativePath;
   }

   @Override
   public String toString() {
      return this.path.toAbsolutePath().toString();
   }
}
