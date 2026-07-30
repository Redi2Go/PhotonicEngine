package at.redi2go.photonics.client.rendering.opengl.objects;

import at.redi2go.photonics.client.rendering.util.BufferUtils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import org.lwjgl.opengl.GL11;

public abstract class TextureObject implements Destructable {
   protected final int[] dimensions;
   protected final int format;
   protected final InternalTextureFormat internalTextureFormat;
   protected final int target;
   protected int textureUnit;
   protected int textureId;
   protected boolean image;

   public TextureObject(int[] dimensions, int format, int target, int textureId, boolean image) {
      this.dimensions = dimensions;
      this.format = format;
      this.target = target;
      this.textureId = textureId;
      this.image = image;
      InternalTextureFormat fFinal = null;

      for (InternalTextureFormat f : InternalTextureFormat.values()) {
         if (f.getGlFormat() == format) {
            fFinal = f;
         }
      }

      this.internalTextureFormat = fFinal;
   }

   public abstract void bind();

   public abstract void unbind();

   public abstract void clear(int var1);

   public abstract void updatePerFrame();

   public BufferedImage download() {
      if (this.getTextureDimensions().length > 2) {
         return null;
      }

      this.bind();
      IntBuffer buffer = BufferUtils.createIntBuffer(4);
      GL11.glGetTexLevelParameteriv(this.target, 0, 4096, buffer);
      int width = buffer.get();
      buffer.position(0);
      GL11.glGetTexLevelParameteriv(this.target, 0, 4097, buffer);
      int height = buffer.get();
      ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * this.getChannelCount());
      GL11.glGetTexImage(3553, 0, 6408, 5121, pixels);
      BufferedImage image = new BufferedImage(width, height, 2);

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            int channelCount = this.getChannelCount();
            pixels.position((x + y * width) * channelCount);
            int r = 0;
            int g = 0;
            int b = 0;
            int a = 255;
            if (channelCount >= 1) {
               r = pixels.get() & 255;
            }

            if (channelCount >= 3) {
               g = pixels.get() & 255;
               b = pixels.get() & 255;
            }

            if (channelCount >= 4) {
               a = pixels.get() & 255;
            }

            image.setRGB(x, y, new Color(r, g, b, a).getRGB());
         }
      }

      this.unbind();
      return image;
   }

   public int[] getTextureDimensions() {
      return Arrays.copyOfRange(this.dimensions, 0, this.dimensions.length - 2);
   }

   public int getChannelCount() {
      return this.dimensions[this.dimensions.length - 2];
   }

   public int getChannelByteCount() {
      return this.dimensions[this.dimensions.length - 1];
   }

   public int getFormat() {
      return this.format;
   }

   public int getTarget() {
      return this.target;
   }

   public int getTextureUnit() {
      return this.textureUnit;
   }

   public void setTextureUnit(int textureUnit) {
      this.textureUnit = textureUnit;
   }

   public int getTextureId() {
      return this.textureId;
   }

   public InternalTextureFormat getInternalTextureFormat() {
      return this.internalTextureFormat;
   }

   public boolean isImage() {
      return this.image;
   }
}
