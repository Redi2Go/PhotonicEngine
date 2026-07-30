package at.redi2go.photonics.client.rendering.opengl.objects;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL44;

public class AtomicIntegerImage extends TextureObject {
   public static final int FORMAT_SIZED = 33334;
   public static final int FORMAT = 36244;
   public static final int TYPE = 5125;
   private final Supplier<Vector3f> resolutionSupplier;

   public AtomicIntegerImage(Supplier<Vector3f> resolutionSupplier) {
      super(new int[]{0, 0, 0, 1, 4}, 33334, 32879, GL11.glGenTextures(), true);
      this.resolutionSupplier = resolutionSupplier;
      GL11.glBindTexture(32879, this.getTextureId());
      GL11.glTexParameteri(32879, 10241, 9728);
      GL11.glTexParameteri(32879, 10240, 9728);
      GL11.glTexParameteri(32879, 10242, 33071);
      GL11.glTexParameteri(32879, 10243, 33071);
      GL11.glTexParameteri(32879, 32882, 33071);
      GL11.glBindTexture(32879, 0);
      this.updatePerFrame();
   }

   @Override
   public void updatePerFrame() {
      Vector3f resolution = this.resolutionSupplier.get();
      if (this.dimensions[0] != resolution.x || this.dimensions[1] != resolution.y || this.dimensions[2] != resolution.z) {
         this.dimensions[0] = (int)Math.max(resolution.x, 1.0F);
         this.dimensions[1] = (int)Math.max(resolution.y, 1.0F);
         this.dimensions[2] = (int)Math.max(resolution.z, 1.0F);
         GL11.glBindTexture(32879, this.getTextureId());
         GL12.glTexImage3D(32879, 0, 33334, this.dimensions[0], this.dimensions[1], this.dimensions[2], 0, 36244, 5125, (ByteBuffer)null);
         GL11.glBindTexture(32879, 0);
      }
   }

   @Override
   public void bind() {
      GL42.glBindImageTexture(this.getTextureUnit(), this.getTextureId(), 0, true, 0, 35002, 33334);
   }

   @Override
   public void unbind() {
      GL42.glBindImageTexture(this.getTextureUnit(), 0, 0, true, 0, 35002, 33334);
   }

   @Override
   public void clear(int value) {
      IntBuffer buffer = BufferUtils.createIntBuffer(1);
      buffer.put(value);
      buffer.flip();
      GL44.glClearTexImage(this.getTextureId(), 0, 36244, 5125, buffer);
   }

   @Override
   public void free() {
      GL11.glDeleteTextures(this.getTextureId());
   }
}
