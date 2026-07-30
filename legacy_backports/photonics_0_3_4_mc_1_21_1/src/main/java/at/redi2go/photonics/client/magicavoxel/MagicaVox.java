package at.redi2go.photonics.client.magicavoxel;

import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

public class MagicaVox {
   public static ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
   public static MagicaVox.Palette DEFAULT_PALETTE = new MagicaVox.Palette();

   public static int packNormalized(float[] unpacked) {
      int xByte = (int)(unpacked[0] * 255.0F);
      int yByte = (int)(unpacked[1] * 255.0F);
      int zByte = (int)(unpacked[2] * 255.0F);
      int wByte = (int)(unpacked[3] * 255.0F);
      return xByte | yByte << 8 | zByte << 16 | wByte << 24;
   }

   public static float[] unpackNormalized(int packed) {
      return new float[]{(packed & 0xFF) / 255.0F, (packed >> 8 & 0xFF) / 255.0F, (packed >> 16 & 0xFF) / 255.0F, (packed >> 24 & 0xFF) / 255.0F};
   }

   static {
      DEFAULT_PALETTE.colors = new int[]{
         0,
         -1,
         -3342337,
         -6684673,
         -10027009,
         -13369345,
         -16711681,
         -13057,
         -3355393,
         -6697729,
         -10040065,
         -13382401,
         -16724737,
         -26113,
         -3368449,
         -6710785,
         -10053121,
         -13395457,
         -16737793,
         -39169,
         -3381505,
         -6723841,
         -10066177,
         -13408513,
         -16750849,
         -52225,
         -3394561,
         -6736897,
         -10079233,
         -13421569,
         -16763905,
         -65281,
         -3407617,
         -6749953,
         -10092289,
         -13434625,
         -16776961,
         -52,
         -3342388,
         -6684724,
         -10027060,
         -13369396,
         -16711732,
         -13108,
         -3355444,
         -6697780,
         -10040116,
         -13382452,
         -16724788,
         -26164,
         -3368500,
         -6710836,
         -10053172,
         -13395508,
         -16737844,
         -39220,
         -3381556,
         -6723892,
         -10066228,
         -13408564,
         -16750900,
         -52276,
         -3394612,
         -6736948,
         -10079284,
         -13421620,
         -16763956,
         -65332,
         -3407668,
         -6750004,
         -10092340,
         -13434676,
         -16777012,
         -103,
         -3342439,
         -6684775,
         -10027111,
         -13369447,
         -16711783,
         -13159,
         -3355495,
         -6697831,
         -10040167,
         -13382503,
         -16724839,
         -26215,
         -3368551,
         -6710887,
         -10053223,
         -13395559,
         -16737895,
         -39271,
         -3381607,
         -6723943,
         -10066279,
         -13408615,
         -16750951,
         -52327,
         -3394663,
         -6736999,
         -10079335,
         -13421671,
         -16764007,
         -65383,
         -3407719,
         -6750055,
         -10092391,
         -13434727,
         -16777063,
         -154,
         -3342490,
         -6684826,
         -10027162,
         -13369498,
         -16711834,
         -13210,
         -3355546,
         -6697882,
         -10040218,
         -13382554,
         -16724890,
         -26266,
         -3368602,
         -6710938,
         -10053274,
         -13395610,
         -16737946,
         -39322,
         -3381658,
         -6723994,
         -10066330,
         -13408666,
         -16751002,
         -52378,
         -3394714,
         -6737050,
         -10079386,
         -13421722,
         -16764058,
         -65434,
         -3407770,
         -6750106,
         -10092442,
         -13434778,
         -16777114,
         -205,
         -3342541,
         -6684877,
         -10027213,
         -13369549,
         -16711885,
         -13261,
         -3355597,
         -6697933,
         -10040269,
         -13382605,
         -16724941,
         -26317,
         -3368653,
         -6710989,
         -10053325,
         -13395661,
         -16737997,
         -39373,
         -3381709,
         -6724045,
         -10066381,
         -13408717,
         -16751053,
         -52429,
         -3394765,
         -6737101,
         -10079437,
         -13421773,
         -16764109,
         -65485,
         -3407821,
         -6750157,
         -10092493,
         -13434829,
         -16777165,
         -256,
         -3342592,
         -6684928,
         -10027264,
         -13369600,
         -16711936,
         -13312,
         -3355648,
         -6697984,
         -10040320,
         -13382656,
         -16724992,
         -26368,
         -3368704,
         -6711040,
         -10053376,
         -13395712,
         -16738048,
         -39424,
         -3381760,
         -6724096,
         -10066432,
         -13408768,
         -16751104,
         -52480,
         -3394816,
         -6737152,
         -10079488,
         -13421824,
         -16764160,
         -65536,
         -3407872,
         -6750208,
         -10092544,
         -13434880,
         -16776978,
         -16776995,
         -16777029,
         -16777046,
         -16777080,
         -16777097,
         -16777131,
         -16777148,
         -16777182,
         -16777199,
         -16716288,
         -16720640,
         -16729344,
         -16733696,
         -16742400,
         -16746752,
         -16755456,
         -16759808,
         -16768512,
         -16772864,
         -1179648,
         -2293760,
         -4521984,
         -5636096,
         -7864320,
         -8978432,
         -11206656,
         -12320768,
         -14548992,
         -15663104,
         -1118482,
         -2236963,
         -4473925,
         -5592406,
         -7829368,
         -8947849,
         -11184811,
         -12303292,
         -14540254,
         -15658735
      };
   }

   public static class Chunk {
      public String id;
      public Object content;
      public Map<Class<?>, List<MagicaVox.Chunk>> children;

      public List<MagicaVox.Chunk> get(Class<?> type) {
         return this.children.get(type);
      }
   }

   public static class Palette {
      public static String IDENTIFIER = "RGBA";
      public int[] colors;

      public int get(int i) {
         return this.colors[this.index(i)];
      }

      public void set(int i, int color) {
         this.colors[this.index(i)] = color;
      }

      private int index(int i) {
         return i - 1;
      }
   }

   public static class Size {
      public static String IDENTIFIER = "SIZE";
      public int width;
      public int height;
      public int depth;
   }

   public static class Vox {
      public int version;
      public MagicaVox.Chunk main;
   }

   public static class Voxels {
      public static String IDENTIFIER = "XYZI";
      public MagicaVox.Size size;
      public byte[] voxels;

      public int get(int x, int y, int z) {
         return this.voxels[this.index(x, y, z)] & 0xFF;
      }

      public void set(int x, int y, int z, int paletteIndex) {
         this.voxels[this.index(x, y, z)] = (byte)paletteIndex;
      }

      private int index(int x, int y, int z) {
         return y * this.size.depth * this.size.width + z * this.size.width + x;
      }
   }
}
