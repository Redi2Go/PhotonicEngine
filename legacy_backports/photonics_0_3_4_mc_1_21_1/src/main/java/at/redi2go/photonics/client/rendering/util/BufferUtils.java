package at.redi2go.photonics.client.rendering.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtils {
   public static ByteBuffer createByteBuffer(int size) {
      return ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
   }

   public static IntBuffer createIntBuffer(int size) {
      return createByteBuffer(size << 2).asIntBuffer();
   }

   public static FloatBuffer createFloatBuffer(int size) {
      return createByteBuffer(size << 2).asFloatBuffer();
   }

   public static float[] array(FloatBuffer buffer) {
      float[] ar = new float[buffer.position()];
      buffer.flip();
      buffer.put(ar);
      return ar;
   }

   public static Float[] arrayBoxed(FloatBuffer buffer) {
      Float[] ar = new Float[buffer.position()];
      buffer.flip();

      for (int i = 0; i < ar.length; i++) {
         ar[i] = buffer.get();
      }

      return ar;
   }

   public static int[] array(IntBuffer buffer) {
      int[] ar = new int[buffer.position()];
      buffer.flip();
      buffer.put(ar);
      return ar;
   }

   public static Integer[] arrayBoxed(IntBuffer buffer) {
      Integer[] ar = new Integer[buffer.position()];
      buffer.flip();

      for (int i = 0; i < ar.length; i++) {
         ar[i] = buffer.get();
      }

      return ar;
   }

   public static int[] packUnorm4x8(float... data) {
      int[] bytes = new int[data.length];

      for (int i = 0; i < data.length; i++) {
         bytes[i] = (int)Math.round(Math.clamp(data[i], 0.0, 1.0) * 255.0);
      }

      return bytes;
   }
}
