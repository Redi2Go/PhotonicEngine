package at.redi2go.photonics.client.rendering.opengl.rendering;

import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.rendering.opengl.objects.GLMemoryCollection;
import at.redi2go.photonics.client.rendering.world.buffer.GlMemoryManager;
import java.util.List;
import java.util.Map.Entry;

public class PhotonicsShader {
   private List<Entry<Integer, GlMemoryManager>> foundGlMemories;
   private final String fragmentName;
   private final String vertexName;
   private final GLMemoryCollection memoryCollection;
   private final ColorFramebuffer framebuffer;

   public PhotonicsShader(String fragmentName, String vertexName, GLMemoryCollection memoryCollection, ColorFramebuffer framebuffer) {
      this.fragmentName = fragmentName;
      this.vertexName = vertexName;
      this.memoryCollection = memoryCollection;
      this.framebuffer = framebuffer;
   }

   public void bindFramebuffer() {
      Raytracer.CURRENT_FRAMEBUFFER = this.framebuffer;
   }

   public String getFragmentName() {
      return this.fragmentName;
   }

   public String getVertexName() {
      return this.vertexName;
   }
}
