package at.redi2go.photonics.client.magicavoxel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import at.redi2go.photonics.client.rendering.schematics.Schematic;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class VoxReaderTest {
   @Test
   void parsesEveryBundledSchematic() throws Exception {
      URI directoryUri = getClass().getResource("/assets/minecraft/schematics").toURI();
      Path directory = Path.of(directoryUri);
      List<Path> schematics;

      try (var paths = Files.list(directory)) {
         schematics = paths.filter(path -> path.getFileName().toString().endsWith(".vox")).sorted().toList();
      }

      assertFalse(schematics.isEmpty());
      for (Path path : schematics) {
         try (InputStream input = Files.newInputStream(path)) {
            MagicaVox.Vox vox = VoxReader.readToVox(input);
            assertNotNull(vox, path.toString());
            assertNotNull(vox.main.get(MagicaVox.Size.class), path.toString());
            assertNotNull(vox.main.get(MagicaVox.Palette.class), path.toString());
            assertNotNull(vox.main.get(MagicaVox.Voxels.class), path.toString());
         }

         try (InputStream input = Files.newInputStream(path)) {
            Schematic schematic = VoxReader.readToSchematic(input);
            assertTrue(schematic.getWidth() > 0, path.toString());
            assertTrue(schematic.getHeight() > 0, path.toString());
            assertTrue(schematic.getDepth() > 0, path.toString());
            assertTrue(schematic.getData().length > 0, path.toString());
         }
      }
   }
}
