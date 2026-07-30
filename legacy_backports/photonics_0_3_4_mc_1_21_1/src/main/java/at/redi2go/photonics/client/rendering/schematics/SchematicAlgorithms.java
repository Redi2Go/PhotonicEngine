package at.redi2go.photonics.client.rendering.schematics;

import java.util.LinkedList;
import java.util.Queue;
import org.joml.Vector3f;

public class SchematicAlgorithms {
   public static boolean canOcclude(Schematic schematic) {
      boolean hasAir = false;

      for (int z = 0; z < schematic.getDepth(); z++) {
         for (int y = 0; y < schematic.getHeight(); y++) {
            if (schematic.getUncheckedEntry(0, y, z) > 0) {
               hasAir = true;
            }

            if (schematic.getUncheckedEntry(schematic.getWidth() - 1, y, z) > 0) {
               hasAir = true;
            }
         }
      }

      for (int z = 0; z < schematic.getDepth(); z++) {
         for (int x = 0; x < schematic.getWidth(); x++) {
            if (schematic.getUncheckedEntry(x, 0, z) > 0) {
               hasAir = true;
            }

            if (schematic.getUncheckedEntry(x, schematic.getHeight() - 1, z) > 0) {
               hasAir = true;
            }
         }
      }

      for (int y = 0; y < schematic.getHeight(); y++) {
         for (int x = 0; x < schematic.getWidth(); x++) {
            if (schematic.getUncheckedEntry(x, y, 0) > 0) {
               hasAir = true;
            }

            if (schematic.getUncheckedEntry(x, y, schematic.getDepth() - 1) > 0) {
               hasAir = true;
            }
         }
      }

      return !hasAir;
   }

   static void cullInside(Schematic schematic) {
      boolean[] visible = new boolean[schematic.data.length];

      for (int x = 0; x < schematic.width; x++) {
         for (int y = 0; y < schematic.height; y++) {
            visibilityFill(schematic, visible, x, y, 0);
         }
      }

      for (int x = 0; x < schematic.width; x++) {
         for (int y = 0; y < schematic.height; y++) {
            visibilityFill(schematic, visible, x, y, schematic.depth - 1);
         }
      }

      for (int x = 0; x < schematic.width; x++) {
         for (int z = 0; z < schematic.depth; z++) {
            visibilityFill(schematic, visible, x, 0, z);
         }
      }

      for (int x = 0; x < schematic.width; x++) {
         for (int z = 0; z < schematic.depth; z++) {
            visibilityFill(schematic, visible, x, schematic.depth - 1, z);
         }
      }

      for (int y = 0; y < schematic.height; y++) {
         for (int z = 0; z < schematic.depth; z++) {
            visibilityFill(schematic, visible, 0, y, z);
         }
      }

      for (int y = 0; y < schematic.height; y++) {
         for (int z = 0; z < schematic.depth; z++) {
            visibilityFill(schematic, visible, schematic.depth - 1, y, z);
         }
      }

      for (int i = 0; i < schematic.data.length; i++) {
         if (!visible[i] && schematic.data[i] == 0) {
            schematic.data[i] = 1;
         }
      }
   }

   static void setup(Schematic schematic) {
      for (int x = 0; x < schematic.width; x++) {
         for (int y = 0; y < schematic.height; y++) {
            for (int z = 0; z < schematic.depth; z++) {
               int idx = Schematic.toSchematicIndex(x, y, z);
               int id = schematic.data[idx];
               schematic.data[idx] = id != 0 ? -id : AirEntry.toAirEntry(x, y, z, x + 1, y + 1, z + 1);
            }
         }
      }
   }

   private static void visibilityFill(Schematic schematic, boolean[] visible, int x0, int y0, int z0) {
      Queue<Vector3f> queue = new LinkedList<>();
      queue.add(new Vector3f(x0, y0, z0));

      while (!queue.isEmpty()) {
         Vector3f v = queue.poll();
         int x = (int)v.x;
         int y = (int)v.y;
         int z = (int)v.z;
         if (x >= 0 && x < schematic.width && y >= 0 && y < schematic.height && z >= 0 && z < schematic.depth) {
            int index = Schematic.toSchematicIndex(x, y, z);
            if (!visible[index] && schematic.data[index] == 0) {
               visible[index] = true;
               queue.add(new Vector3f(x - 1, y, z));
               queue.add(new Vector3f(x + 1, y, z));
               queue.add(new Vector3f(x, y - 1, z));
               queue.add(new Vector3f(x, y + 1, z));
               queue.add(new Vector3f(x, y, z - 1));
               queue.add(new Vector3f(x, y, z + 1));
            }
         }
      }
   }

   static void optimize(Schematic schematic, int x) {
      for (int y = 0; y < schematic.getHeight(); y++) {
         for (int z = 0; z < schematic.getDepth(); z++) {
            int airEntry1 = schematic.getUncheckedEntry(x, y, z);
            if (!AirEntry.isData(airEntry1)) {
               schematic.setEntry(x, y, z, mergeNeighbours(schematic, airEntry1));
            }
         }
      }
   }

   private static int mergeNeighbours(Schematic schematic, int airEntry) {
      int startX = AirEntry.getX1(airEntry);
      int startY = AirEntry.getY1(airEntry);
      int startZ = AirEntry.getZ1(airEntry);
      int endX = AirEntry.getX2(airEntry);
      int endY = AirEntry.getY2(airEntry);
      int endZ = AirEntry.getZ2(airEntry);
      boolean northMerged = true;
      boolean southMerged = true;
      boolean eastMerged = true;
      boolean westMerged = true;
      boolean topMerged = true;
      boolean bottomMerged = true;

      boolean merged;
      do {
         merged = false;
         if (northMerged && canMergeNorthSouth(schematic, startX, startY, endZ, endX, endY)) {
            endZ++;
            merged = true;
         } else {
            northMerged = false;
         }

         if (southMerged && canMergeNorthSouth(schematic, startX, startY, startZ - 1, endX, endY)) {
            startZ--;
            merged = true;
         } else {
            southMerged = false;
         }

         if (eastMerged && canMergeEastWest(schematic, endX, startY, startZ, endY, endZ)) {
            endX++;
            merged = true;
         } else {
            eastMerged = false;
         }

         if (westMerged && canMergeEastWest(schematic, startX - 1, startY, startZ, endY, endZ)) {
            startX--;
            merged = true;
         } else {
            westMerged = false;
         }

         if (topMerged && canMergeTopBottom(schematic, startX, endY, startZ, endX, endZ)) {
            endY++;
            merged = true;
         } else {
            topMerged = false;
         }

         if (bottomMerged && canMergeTopBottom(schematic, startX, startY - 1, startZ, endX, endZ)) {
            startY--;
            merged = true;
         } else {
            bottomMerged = false;
         }
      } while (merged && surfaceArea(startX, startY, startZ, endX, endY, endZ) < 300);

      return AirEntry.toAirEntry(startX, startY, startZ, endX, endY, endZ);
   }

   private static boolean canMergeEastWest(Schematic schematic, int x, int startY, int startZ, int endY, int endZ) {
      if (!schematic.isInBounds(x, startY, startZ)) {
         return false;
      }

      for (int y = startY; y < endY; y++) {
         for (int z = startZ; z < endZ; z++) {
            if (AirEntry.isData(schematic.getUncheckedEntry(x, y, z))) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean canMergeNorthSouth(Schematic schematic, int startX, int startY, int z, int endX, int endY) {
      if (!schematic.isInBounds(startX, startY, z)) {
         return false;
      }

      for (int x = startX; x < endX; x++) {
         for (int y = startY; y < endY; y++) {
            if (AirEntry.isData(schematic.getUncheckedEntry(x, y, z))) {
               return false;
            }
         }
      }

      return true;
   }

   private static boolean canMergeTopBottom(Schematic schematic, int startX, int y, int startZ, int endX, int endZ) {
      if (!schematic.isInBounds(startX, y, startZ)) {
         return false;
      }

      for (int x = startX; x < endX; x++) {
         for (int z = startZ; z < endZ; z++) {
            if (AirEntry.isData(schematic.getUncheckedEntry(x, y, z))) {
               return false;
            }
         }
      }

      return true;
   }

   private static int surfaceArea(int x1, int y1, int z1, int x2, int y2, int z2) {
      int dx = x2 - x1;
      int dy = y2 - y1;
      int dz = z2 - z1;
      return 2 * (dx * dy + dx * dz + dy * dz);
   }
}
