package at.redi2go.photonics.client.rendering.schematics;

import at.redi2go.photonics.client.rendering.util.MultiThreader;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.joml.Vector3f;

public class Schematic {
   public static final int UNINITIALIZED = 0;
   public static final int INITIALIZED = 1;
   public static final int OPTIMIZED = 2;
   int[] data;
   int width;
   int height;
   int depth;
   int state;
   int[] hashVector;

   public Schematic(int width, int height, int depth) {
      this(new int[width * height * depth], width, height, depth);
   }

   public Schematic(int[] data, int width, int height, int depth) {
      if (!arePowerOf2Dimensions(width, height, depth)) {
         throw new IllegalArgumentException();
      }

      this.data = data;
      this.width = width;
      this.height = height;
      this.depth = depth;
      this.state = 0;
   }

   public void initialize() {
      if (this.state != 0) {
         throw new IllegalStateException();
      }

      SchematicAlgorithms.setup(this);
      this.state = 1;
   }

   public void cullInside() {
      if (this.state != 1) {
         throw new IllegalStateException();
      }

      SchematicAlgorithms.cullInside(this);
   }

   public CompletableFuture<Void> optimizeThreaded() {
      if (this.state != 1) {
         throw new IllegalStateException();
      } else {
         return MultiThreader.run(this.getWidth(), x -> {
            SchematicAlgorithms.optimize(this, x);
            this.state = 2;
         });
      }
   }

   public Schematic transform(Consumer<Vector3f> transformer) {
      Schematic transformed = new Schematic(new int[this.data.length], this.width, this.height, this.depth);

      for (int x = 0; x < this.width; x++) {
         for (int y = 0; y < this.height; y++) {
            for (int z = 0; z < this.depth; z++) {
               Vector3f transformedVector = new Vector3f(x, y, z);
               transformer.accept(transformedVector);
               transformed.setEntry((int)transformedVector.x, (int)transformedVector.y, (int)transformedVector.z, this.getEntry(x, y, z));
            }
         }
      }

      return transformed;
   }

   public void reset() {
      this.state = 0;
   }

   public void calcRotationHash() {
      if (this.state == 0 && this.width == 16 && this.height == 16 && this.depth == 16) {
         int hashX = 0;
         int hashY = 0;
         int hashZ = 0;
         int valueHashMask = 32767;

         for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
               for (int z = 0; z < 16; z++) {
                  int hashedValue = hash(this.getEntry(x, y, z) + -217138435) & valueHashMask;
                  hashX += hashedValue * fromCenter(x);
                  hashY += hashedValue * fromCenter(y);
                  hashZ += hashedValue * fromCenter(z);
               }
            }
         }

         this.hashVector = new int[]{hashX, hashY, hashZ};
      } else {
         throw new IllegalStateException();
      }
   }

   private static int fromCenter(int value) {
      return (value > 7 ? 1 : 0) + value - 8;
   }

   private static int hash(int x) {
      x = (x >> 16 ^ x) * 73244475;
      x = (x >> 16 ^ x) * 73244475;
      return x >> 16 ^ x;
   }

   public int getUncheckedEntry(int x, int y, int z) {
      return this.data[toSchematicIndex(x, y, z)];
   }

   public int getEntry(int x, int y, int z) {
      if (!this.isInBounds(x, y, z)) {
         assert false;
         return Integer.MAX_VALUE;
      } else {
         return this.data[toSchematicIndex(x, y, z)];
      }
   }

   public boolean isInBounds(int x, int y, int z) {
      return x >= 0 && x < this.width && y >= 0 && y < this.height && z >= 0 && z < this.depth;
   }

   public void setEntry(int x, int y, int z, int data) {
      this.data[toSchematicIndex(x, y, z)] = data;
   }

   public static int expandBits(int value) {
      int v = value & 31;
      v = (v | v << 16) & 50331903;
      v = (v | v << 8) & 50393103;
      v = (v | v << 4) & 51130563;
      return (v | v << 2) & 153391689;
   }

   public static int toSchematicIndex(int x, int y, int z) {
      return expandBits(x) | expandBits(y) << 1 | expandBits(z) << 2;
   }

   public int[] getHashVector() {
      return this.hashVector;
   }

   public int getState() {
      return this.state;
   }

   public void setState(int state) {
      this.state = state;
   }

   public int[] getData() {
      return this.data;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public int getDepth() {
      return this.depth;
   }

   public static boolean arePowerOf2Dimensions(int width, int height, int depth) {
      return isPowerOf2(width) && isPowerOf2(height) && isPowerOf2(depth);
   }

   private static boolean isPowerOf2(int n) {
      return n > 0 && (n & n - 1) == 0;
   }
}
