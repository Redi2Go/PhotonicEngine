package at.redi2go.photonics.client.rendering.opengl.objects;

import com.mojang.blaze3d.platform.GlStateManager;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL44;

public class Texture2D extends TextureObject {
   private final int width;
   private final int height;
   private final boolean pixelated;
   private final boolean alpha;

   public Texture2D(int width, int height, boolean pixelated, boolean highBitDepth, boolean alpha) {
      this(width, height, pixelated, highBitDepth, alpha, loadImage(null, getFormat(highBitDepth, alpha), width, height, pixelated, alpha));
   }

   public Texture2D(BufferedImage image, boolean pixelated, boolean highBitDepth, boolean alpha) {
      this(
         image.getWidth(),
         image.getHeight(),
         pixelated,
         highBitDepth,
         alpha,
         loadImage(image, getFormat(highBitDepth, alpha), image.getWidth(), image.getHeight(), pixelated, alpha)
      );
   }

   public Texture2D(int width, int height, boolean pixelated, boolean highBitDepth, boolean alpha, int textureId) {
      super(new int[]{width, height, alpha ? 4 : 3, highBitDepth ? 4 : 1}, getFormat(highBitDepth, alpha), 3553, textureId, false);
      this.width = width;
      this.height = height;
      this.pixelated = pixelated;
      this.alpha = alpha;
   }

   private static int getFormat(boolean highBitDepth, boolean alpha) {
      if (highBitDepth) {
         return alpha ? 34836 : 34837;
      } else {
         return alpha ? 32856 : 32849;
      }
   }

   private static int loadImage(BufferedImage image, int format, int width, int height, boolean pixelated, boolean alpha) {
      IntBuffer textureBuffer = null;
      if (image != null) {
         int[] data = new int[(alpha ? 4 : 3) * image.getWidth() * image.getHeight()];
         image.getRaster().getPixels(0, 0, image.getWidth(), image.getHeight(), data);
         textureBuffer = ByteBuffer.allocateDirect(16 * data.length).asIntBuffer();

         for (int component : data) {
            textureBuffer.put(component);
         }

         textureBuffer.flip();
      }

      int textureId = GL11.glGenTextures();
      GL11.glBindTexture(3553, textureId);
      GL11.glTexImage2D(3553, 0, format, width, height, 0, 6408, 5125, textureBuffer);
      GL11.glTexParameteri(3553, 10241, pixelated ? 9728 : 9729);
      GL11.glTexParameteri(3553, 10240, pixelated ? 9728 : 9729);
      GL11.glBindTexture(3553, 0);
      return textureId;
   }

   @Override
   public void bind() {
      GlStateManager._activeTexture(33984 + this.getTextureUnit());
      GlStateManager._bindTexture(this.getTextureId());
   }

   @Override
   public void unbind() {
      GL11.glBindTexture(3553, 0);
   }

   @Override
   public void clear(int i) {
      GL44.glClearTexImage(this.getTextureId(), 0, 6408, 5125, (ByteBuffer)null);
   }

   @Override
   public void updatePerFrame() {
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean isPixelated() {
      return this.pixelated;
   }

   public boolean isAlpha() {
      return this.alpha;
   }

   @Override
   public void free() {
      GL11.glDeleteTextures(this.textureId);
   }
}
