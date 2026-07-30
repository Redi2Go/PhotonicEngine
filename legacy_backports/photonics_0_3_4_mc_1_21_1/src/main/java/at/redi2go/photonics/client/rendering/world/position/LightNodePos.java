package at.redi2go.photonics.client.rendering.world.position;

import java.util.Objects;

public class LightNodePos {
   public int x;
   public int y;
   public int z;

   public LightNodePos(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public LightNodePos(LightNodePos pos) {
      this.x = pos.x;
      this.y = pos.y;
      this.z = pos.z;
   }

   public void add(int x, int y, int z) {
      this.x += x;
      this.y += y;
      this.z += z;
   }

   public void add(int scalar) {
      this.x += scalar;
      this.y += scalar;
      this.z += scalar;
   }

   public void sub(int x, int y, int z) {
      this.x -= x;
      this.y -= y;
      this.z -= z;
   }

   public void sub(int scalar) {
      this.x -= scalar;
      this.y -= scalar;
      this.z -= scalar;
   }

   public PBlockPos toBlockPos(int nodeSize, PBlockPos offset) {
      return new PBlockPos(this.x * nodeSize + offset.x, this.y * nodeSize + offset.y, this.z * nodeSize + offset.z);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         LightNodePos that = (LightNodePos)object;
         return this.x == that.x && this.y == that.y && this.z == that.z;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
