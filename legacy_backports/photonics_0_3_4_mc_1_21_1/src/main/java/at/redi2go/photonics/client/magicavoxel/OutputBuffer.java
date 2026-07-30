package at.redi2go.photonics.client.magicavoxel;

import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;

public class OutputBuffer {
   private final ByteOrder order;
   private final OutputBuffer.OutputArray array = new OutputBuffer.OutputArray();

   public OutputBuffer(ByteOrder order) {
      this.order = order;
   }

   public void put(byte b) {
      this.array.write(b);
   }

   public void putInt(int i) {
      this.array.write(i & 0xFF);
      this.array.write(i >> 8 & 0xFF);
      this.array.write(i >> 16 & 0xFF);
      this.array.write(i >> 24 & 0xFF);
   }

   public void put(byte[] src) {
      for (int i = 0; i < src.length; i++) {
         this.put(src[i]);
      }
   }

   public void putInt(int[] src) {
      for (int i = 0; i < src.length; i++) {
         this.putInt(src[i]);
      }
   }

   public byte[] array() {
      return this.array.toByteArray();
   }

   public int getIndex() {
      return this.array.getIndex();
   }

   private static class OutputArray extends ByteArrayOutputStream {
      public int getIndex() {
         return this.count;
      }
   }
}
