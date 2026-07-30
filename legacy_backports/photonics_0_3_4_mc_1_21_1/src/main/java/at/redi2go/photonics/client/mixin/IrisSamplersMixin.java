package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.samplers.IrisSamplers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisSamplers.class, remap = false)
public abstract class IrisSamplersMixin {
   @Inject(method = "addCustomTextures", at = @At("TAIL"))
   private static void addCustomTexture(SamplerHolder samplers, Object2ObjectMap<String, TextureAccess> irisCustomTextures, CallbackInfo ci) {
      if (Raytracer.shouldBeEnabled()) {
         Raytracer.INSTANCE.getMainRenderer().registerCustomTextures(samplers);
      }
   }
}
