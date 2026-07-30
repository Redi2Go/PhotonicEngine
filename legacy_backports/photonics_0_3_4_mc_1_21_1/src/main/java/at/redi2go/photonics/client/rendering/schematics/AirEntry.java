package at.redi2go.photonics.client.rendering.schematics;

public class AirEntry {
   public static boolean isAirEntry(int airEntry) {
      return airEntry != Integer.MAX_VALUE && airEntry > 0;
   }

   public static boolean isData(int airEntry) {
      return airEntry <= 0;
   }

   public static int toData(int data) {
      return -data;
   }

   public static int fromData(int data) {
      return -data;
   }

   public static int toAirEntry(int x1, int y1, int z1, int x2, int y2, int z2) {
      return x1 << 0 | y1 << 5 | z1 << 10 | x2 - 1 << 15 | y2 - 1 << 20 | z2 - 1 << 25;
   }

   public static int getX1(int airEntry) {
      return airEntry >> 0 & 31;
   }

   public static int getY1(int airEntry) {
      return airEntry >> 5 & 31;
   }

   public static int getZ1(int airEntry) {
      return airEntry >> 10 & 31;
   }

   public static int getX2(int airEntry) {
      return (airEntry >> 15 & 31) + 1;
   }

   public static int getY2(int airEntry) {
      return (airEntry >> 20 & 31) + 1;
   }

   public static int getZ2(int airEntry) {
      return (airEntry >> 25 & 31) + 1;
   }
}
