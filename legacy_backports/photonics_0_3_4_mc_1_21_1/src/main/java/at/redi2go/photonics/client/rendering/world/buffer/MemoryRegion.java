package at.redi2go.photonics.client.rendering.world.buffer;

import java.nio.ByteBuffer;

public class MemoryRegion {
   private final ByteBuffer buffer;
   public final int begin;
   public final int end;
   public boolean allocated = true;

   MemoryRegion(ByteBuffer buffer, int begin, int end) {
      this.buffer = buffer;
      this.begin = begin;
      this.end = end;
   }

   public ByteBuffer getBuffer() {
      return this.buffer.duplicate().order(this.buffer.order());
   }
}
