package at.redi2go.photonics.client;

import at.redi2go.photonics.client.rendering.util.BufferUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriteContext {
   private final ZipOutputStream zipOutputStream;
   private final ByteBuffer buffer;
   private final byte[] bufferArray;

   public ZipWriteContext(ZipOutputStream zipOutputStream, int byteSize) {
      this.zipOutputStream = zipOutputStream;
      this.buffer = BufferUtils.createByteBuffer(byteSize);
      this.bufferArray = new byte[byteSize];
   }

   public void write(String fileName, String[] array) {
      this.write(fileName, String.join("\n", array).getBytes());
   }

   public void write(String fileName, int[] array) {
      this.buffer.asIntBuffer().put(0, array);
      this.buffer.get(0, this.bufferArray);
      this.write(fileName, this.bufferArray);
   }

   public void write(String fileName, byte[] array) {
      try {
         this.zipOutputStream.putNextEntry(new ZipEntry(fileName));
         this.zipOutputStream.write(array);
         this.zipOutputStream.closeEntry();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public ZipOutputStream getZipOutputStream() {
      return this.zipOutputStream;
   }
}
