package at.redi2go.photonics.client.magicavoxel;

import at.redi2go.photonics.client.rendering.schematics.Schematic;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class VoxWriter {
   private static final Map<Class<?>, Integer> CHUNK_WRITE_ORDER = Map.of(MagicaVox.Voxels.class, 1, MagicaVox.Palette.class, 2);

   public static void writeFromSchematic(Schematic schematic, OutputStream outputStream) {
      if (schematic.getState() == 0) {
         MagicaVox.Size size = new MagicaVox.Size();
         size.width = schematic.getWidth();
         size.height = schematic.getHeight();
         size.depth = schematic.getDepth();
         MagicaVox.Chunk sizeChunk = new MagicaVox.Chunk();
         sizeChunk.id = MagicaVox.Size.IDENTIFIER;
         sizeChunk.content = size;
         MagicaVox.Voxels voxels = new MagicaVox.Voxels();
         voxels.size = size;
         voxels.voxels = new byte[size.width * size.height * size.depth];
         MagicaVox.Chunk voxelChunk = new MagicaVox.Chunk();
         voxelChunk.id = MagicaVox.Voxels.IDENTIFIER;
         voxelChunk.content = voxels;
         MagicaVox.Palette palette = new MagicaVox.Palette();
         palette.colors = new int[256];
         populateVoxels(schematic, voxels, palette, 0.0F);
         MagicaVox.Chunk paletteChunk = new MagicaVox.Chunk();
         paletteChunk.id = MagicaVox.Palette.IDENTIFIER;
         paletteChunk.content = palette;
         MagicaVox.Chunk root = new MagicaVox.Chunk();
         root.id = "MAIN";
         root.children = Map.of(
            MagicaVox.Size.class, List.of(sizeChunk), MagicaVox.Voxels.class, List.of(voxelChunk), MagicaVox.Palette.class, List.of(paletteChunk)
         );
         MagicaVox.Vox vox = new MagicaVox.Vox();
         vox.version = 200;
         vox.main = root;
         writeFromVox(vox, outputStream);
      }
   }

   public static void writeFromVox(MagicaVox.Vox vox, OutputStream outputStream) {
      OutputBuffer buffer = new OutputBuffer(MagicaVox.BYTE_ORDER);
      writeString(buffer, "VOX ");
      buffer.putInt(vox.version);
      buffer.put(writeChunk(vox.main).array());

      try {
         outputStream.write(buffer.array());
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private static OutputBuffer writeChunk(MagicaVox.Chunk chunk) {
      OutputBuffer buffer = new OutputBuffer(MagicaVox.BYTE_ORDER);
      writeString(buffer, chunk.id);
      OutputBuffer contentBuffer = writeContent(chunk.id, chunk.content);
      OutputBuffer childrenBuffer = new OutputBuffer(MagicaVox.BYTE_ORDER);
      if (chunk.children != null) {
         chunk.children
            .entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> CHUNK_WRITE_ORDER.getOrDefault(entry.getKey(), 0)))
            .forEach(entry -> entry.getValue().forEach(c -> childrenBuffer.put(writeChunk(c).array())));
      }

      buffer.putInt(contentBuffer.getIndex());
      buffer.putInt(childrenBuffer.getIndex());
      buffer.put(contentBuffer.array());
      buffer.put(childrenBuffer.array());
      return buffer;
   }

   private static OutputBuffer writeContent(String id, Object content) {
      OutputBuffer buffer = new OutputBuffer(MagicaVox.BYTE_ORDER);
      switch (id) {
         case "SIZE": {
            MagicaVox.Size size = (MagicaVox.Size)content;
            buffer.putInt(size.width);
            buffer.putInt(size.depth);
            buffer.putInt(size.height);
            break;
         }
         case "XYZI": {
            MagicaVox.Voxels voxels = (MagicaVox.Voxels)content;
            MagicaVox.Size size = voxels.size;
            int voxelCount = 0;

            for (byte b : voxels.voxels) {
               if (b != 0) {
                  voxelCount++;
               }
            }

            buffer.putInt(voxelCount);

            for (int x = 0; x < size.width; x++) {
               for (int y = 0; y < size.height; y++) {
                  for (int z = 0; z < size.depth; z++) {
                     int palettex = voxels.get(x, y, z);
                     if (palettex != 0) {
                        buffer.put((byte)x);
                        buffer.put((byte)z);
                        buffer.put((byte)y);
                        buffer.put((byte)palettex);
                     }
                  }
               }
            }
            break;
         }
         case "RGBA":
            MagicaVox.Palette palette = (MagicaVox.Palette)content;
            buffer.putInt(palette.colors);
         case "MAIN":
         case "nTRN":
         case "nGRP":
         case "nSHP":
         case "LAYR":
         case "MATL":
         case "rOBJ":
         case "rCAM":
         case "NOTE":
            break;
         default:
            throw new IllegalStateException();
      }

      return buffer;
   }

   private static void writeString(OutputBuffer buffer, String string) {
      buffer.put(string.getBytes());
   }

   private static void populateVoxels(Schematic schematic, MagicaVox.Voxels voxels, MagicaVox.Palette palette, float minDistance) {
      MagicaVox.Size size = voxels.size;
      Map<float[], Integer> palettes = new HashMap<>();

      for (int x = 0; x < size.width; x++) {
         for (int y = 0; y < size.height; y++) {
            for (int z = 0; z < size.depth; z++) {
               int entry = schematic.getEntry(x, y, z);
               if (entry != 0) {
                  float[] color = MagicaVox.unpackNormalized(entry);
                  color[3] *= 2.0F;
                  int index = -1;
                  float bestDistance = Float.POSITIVE_INFINITY;

                  for (Entry<float[], Integer> paletteEntry : palettes.entrySet()) {
                     float[] otherColor = paletteEntry.getKey();
                     float dx = color[0] - otherColor[0];
                     float dy = color[1] - otherColor[1];
                     float dz = color[2] - otherColor[2];
                     float dw = color[3] - otherColor[3];
                     float dist = dx * dx + dy * dy + dz * dz + dw * dw;
                     if (dist <= minDistance && dist < bestDistance) {
                        bestDistance = dist;
                        index = paletteEntry.getValue();
                     }
                  }

                  if (index == -1) {
                     index = palettes.size();
                     if (index > 254) {
                        populateVoxels(schematic, voxels, palette, minDistance != 0.0F ? 10.0F * minDistance : 0.001F);
                     } else {
                        palettes.put(color, index);
                     }
                  }

                  voxels.set(x, y, z, index + 1);
               }
            }
         }
      }

      palettes.forEach((colorx, indexx) -> palette.colors[indexx] = MagicaVox.packNormalized(colorx));
   }
}
