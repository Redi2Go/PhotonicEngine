package at.redi2go.photonics.client.rendering.opengl.objects;

import com.mojang.blaze3d.platform.Window;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class CubeMapFramebuffer extends TextureObject {
   private final int fbo;
   private final int depthBuffer;
   private final int size;

   public CubeMapFramebuffer(int size) {
      super(new int[]{1, 1, 4, 1}, 6408, 34067, createCubeMapTexture(size), false);
      this.size = size;
      this.fbo = GL30.glGenFramebuffers();
      this.depthBuffer = GL30.glGenRenderbuffers();
   }

   public void render(Consumer<Integer> consumer) {
      GL30.glBindFramebuffer(36160, this.fbo);
      GL11.glDrawBuffer(36064);
      GL30.glBindRenderbuffer(36161, this.depthBuffer);
      GL30.glRenderbufferStorage(36161, 33190, this.size, this.size);
      GL30.glFramebufferRenderbuffer(36160, 36096, 36161, this.depthBuffer);
      GL11.glViewport(0, 0, this.size, this.size);

      for (int i = 0; i < 6; i++) {
         GL30.glFramebufferTexture2D(36160, 36064, 34069 + i, this.getTextureId(), 0);
         consumer.accept(i);
      }

      Window window = Minecraft.getInstance().getWindow();
      GL30.glBindFramebuffer(36160, 0);
      GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
   }

   private static int createCubeMapTexture(int size) {
      int texID = GL11.glGenTextures();
      GL11.glBindTexture(34067, texID);

      for (int i = 0; i < 6; i++) {
         GL11.glTexImage2D(34069 + i, 0, 32856, size, size, 0, 6408, 5121, (ByteBuffer)null);
      }

      GL11.glTexParameteri(34067, 10240, 9729);
      GL11.glTexParameteri(34067, 10241, 9729);
      GL11.glTexParameteri(34067, 10242, 33071);
      GL11.glTexParameteri(34067, 10243, 33071);
      GL11.glTexParameteri(34067, 32882, 33071);
      GL11.glBindTexture(34067, 0);
      return texID;
   }

   @Override
   public void bind() {
      GL13.glActiveTexture(33984 + this.getTextureUnit());
      GL11.glBindTexture(34067, this.getTextureId());
   }

   @Override
   public void unbind() {
      GL11.glBindTexture(34067, 0);
   }

   @Override
   public void clear(int i) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void updatePerFrame() {
   }

   @Override
   public void free() {
      GL30.glDeleteFramebuffers(this.fbo);
      GL30.glDeleteRenderbuffers(this.depthBuffer);
      GL11.glDeleteTextures(this.textureId);
   }
}
