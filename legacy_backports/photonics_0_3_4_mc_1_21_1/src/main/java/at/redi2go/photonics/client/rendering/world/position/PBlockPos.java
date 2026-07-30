package at.redi2go.photonics.client.rendering.world.position;

import java.util.Objects;
import org.joml.Vector3i;

public class PBlockPos {
   public int x;
   public int y;
   public int z;

   public PBlockPos(int x, int y, int z) {
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

   public void min(int x, int y, int z) {
      this.x = Math.min(this.x, x);
      this.y = Math.min(this.y, y);
      this.z = Math.min(this.z, z);
   }

   public void max(int x, int y, int z) {
      this.x = Math.max(this.x, x);
      this.y = Math.max(this.y, y);
      this.z = Math.max(this.z, z);
   }

   public void scale(int x, int y, int z) {
      this.x *= x;
      this.y *= y;
      this.z *= z;
   }

   public PChunkPos toChunkPos() {
      return new PChunkPos(Math.floorDiv(this.x, 16), Math.floorDiv(this.y, 16), Math.floorDiv(this.z, 16));
   }

   public LightNodePos toLightPos(int nodeSize, PBlockPos offset) {
      return new LightNodePos((this.x - offset.x) / nodeSize, (this.y - offset.y) / nodeSize, (this.z - offset.z) / nodeSize);
   }

   public Vector3i toVector() {
      return new Vector3i(this.x, this.y, this.z);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         PBlockPos blockPos = (PBlockPos)object;
         return this.x == blockPos.x && this.y == blockPos.y && this.z == blockPos.z;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
