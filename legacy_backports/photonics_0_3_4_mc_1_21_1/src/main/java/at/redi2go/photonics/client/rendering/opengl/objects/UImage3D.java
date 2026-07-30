package at.redi2go.photonics.client.rendering.opengl.objects;

import at.redi2go.photonics.client.rendering.opengl.GL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL44;

public class UImage3D extends TextureObject {
   private static final int[] FORMATS = new int[]{36244, 33320, 36248, 36249};
   private static final int[] TYPES = new int[]{5121, 5123, -1, 5125};
   private final Supplier<Vector3f> resolutionSupplier;

   public UImage3D(Supplier<Vector3f> resolutionSupplier, String dataType, boolean alpha, int bytesPerChannel, boolean interpolate) {
      super(
         new int[]{0, 0, 0, alpha ? 4 : 3, bytesPerChannel},
         GL.pGetInternalFormat((alpha ? "RGBA" : "RGB") + 8 * bytesPerChannel + dataType),
         32879,
         GL11.glGenTextures(),
         true
      );
      this.resolutionSupplier = resolutionSupplier;
      GL11.glBindTexture(32879, this.getTextureId());
      GL11.glTexParameteri(32879, 10241, interpolate ? 9729 : 9728);
      GL11.glTexParameteri(32879, 10240, interpolate ? 9729 : 9728);
      GL11.glTexParameteri(32879, 10242, 33071);
      GL11.glTexParameteri(32879, 10243, 33071);
      GL11.glTexParameteri(32879, 32882, 33071);
      GL11.glBindTexture(32879, 0);
   }

   @Override
   public void bind() {
      GL11.glBindTexture(32879, this.getTextureId());
      GL42.glBindImageTexture(this.getTextureUnit(), this.getTextureId(), 0, true, 0, 35002, this.getFormat());
   }

   @Override
   public void unbind() {
      GL11.glBindTexture(32879, this.getTextureId());
      GL42.glBindImageTexture(this.getTextureUnit(), 0, 0, true, 0, 35002, this.getFormat());
      GL11.glBindTexture(32879, 0);
   }

   @Override
   public void clear(int value) {
      IntBuffer buffer = BufferUtils.createIntBuffer(1);
      buffer.put(value);
      buffer.flip();
      GL44.glClearTexImage(this.getTextureId(), 0, this.getFormat(), 5125, buffer);
   }

   @Override
   public void updatePerFrame() {
      Vector3f resolution = this.resolutionSupplier.get();
      if (this.dimensions[0] != resolution.x || this.dimensions[1] != resolution.y || this.dimensions[2] != resolution.z) {
         this.dimensions[0] = (int)Math.max(resolution.x, 1.0F);
         this.dimensions[1] = (int)Math.max(resolution.y, 1.0F);
         this.dimensions[2] = (int)Math.max(resolution.z, 1.0F);
         GL12.glTexImage3D(
            32879,
            0,
            this.getFormat(),
            this.dimensions[0],
            this.dimensions[1],
            this.dimensions[2],
            0,
            FORMATS[this.getChannelCount() - 1],
            TYPES[this.getChannelByteCount() - 1],
            (ByteBuffer)null
         );
      }
   }

   @Override
   public void free() {
      GL11.glDeleteTextures(this.textureId);
   }
}
