package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.RenderDispatcher;
import at.redi2go.photonics.client.rendering.opengl.objects.TextureObject;
import java.util.Set;
import java.util.function.Supplier;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.texture.InternalTextureFormat;
import net.irisshaders.iris.samplers.IrisImages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisImages.class, remap = false)
public class IrisImagesMixin {
   @Inject(method = "addCustomImages", at = @At("TAIL"))
   private static void addCustomImages(ImageHolder images, Set<GlImage> customImages, CallbackInfo ci) {
      if (Raytracer.shouldBeEnabled()) {
         addImageSampler(images, "gi_x", InternalTextureFormat.R32UI);
         addImageSampler(images, "gi_y", InternalTextureFormat.R32UI);
         addImageSampler(images, "gi_z", InternalTextureFormat.R32UI);
         addImageSampler(images, "gi_w", InternalTextureFormat.R32UI);
         addImageSampler(images, "gi_d", InternalTextureFormat.R32UI);
      }
   }

   @Unique
   private static void addImageSampler(ImageHolder images, String name, InternalTextureFormat internalTextureFormat) {
      Supplier<RenderDispatcher> renderDispatcher = () -> Raytracer.INSTANCE.getRenderDispatcher();
      images.addTextureImage(() -> {
         TextureObject image = renderDispatcher.get().getTextureObject(name);
         if (image.getInternalTextureFormat() != internalTextureFormat) {
            throw new IllegalStateException();
         }

         image.updatePerFrame();
         return image.getTextureId();
      }, internalTextureFormat, name);
   }
}
