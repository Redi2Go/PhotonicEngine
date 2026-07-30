package at.redi2go.photonics.client.rendering.world.position;

import java.util.Objects;

public class PChunkPos {
   public int x;
   public int y;
   public int z;

   public PChunkPos(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void add(int x, int y, int z) {
      this.x += x;
      this.y += y;
      this.z += z;
   }

   public void sub(int x, int y, int z) {
      this.x -= x;
      this.y -= y;
      this.z -= z;
   }

   public PBlockPos toBlockPos() {
      return new PBlockPos(16 * this.x, 16 * this.y, 16 * this.z);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         PChunkPos chunkPos = (PChunkPos)object;
         return this.x == chunkPos.x && this.y == chunkPos.y && this.z == chunkPos.z;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
