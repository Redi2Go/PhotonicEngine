package at.redi2go.photonics.client.rendering.opengl.objects;

public enum GlTarget {
   SSBO(37074),
   UBO(35345);

   public final int target;

   GlTarget(int target) {
      this.target = target;
   }
}
