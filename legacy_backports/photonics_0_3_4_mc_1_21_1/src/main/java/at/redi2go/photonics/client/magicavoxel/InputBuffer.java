package at.redi2go.photonics.client.magicavoxel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteOrder;

public class InputBuffer {
   private final ByteOrder order;
   private final InputBuffer.InputArray array;

   public InputBuffer(ByteOrder order, byte[] array) {
      if (order != ByteOrder.LITTLE_ENDIAN) {
         throw new UnsupportedOperationException();
      }

      this.order = order;
      this.array = new InputBuffer.InputArray(array);
   }

   public byte get() {
      int i = this.array.read();
      if (i == -1) {
         throw new BufferOverflowException();
      } else {
         return (byte)i;
      }
   }

   public int getInt() {
      return this.array.read() | this.array.read() << 8 | this.array.read() << 16 | this.array.read() << 24;
   }

   public void get(byte[] dst) {
      for (int i = 0; i < dst.length; i++) {
         dst[i] = this.get();
      }
   }

   public void getInt(int[] dst) {
      for (int i = 0; i < dst.length; i++) {
         dst[i] = this.getInt();
      }
   }

   public InputBuffer slice(int length) {
      try {
         return new InputBuffer(this.order, this.array.readNBytes(length));
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public boolean isAvailable() {
      return this.array.available() > 0;
   }

   public int available() {
      return this.array.available();
   }

   public int getIndex() {
      return this.array.getIndex();
   }

   private static class InputArray extends ByteArrayInputStream {
      public InputArray(byte[] buf) {
         super(buf);
      }

      public int getIndex() {
         return this.pos;
      }
   }
}
