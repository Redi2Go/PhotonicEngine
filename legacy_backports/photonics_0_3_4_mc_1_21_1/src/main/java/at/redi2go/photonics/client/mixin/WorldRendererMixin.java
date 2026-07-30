package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
   @Inject(method = "renderLevel", at = @At("HEAD"))
   public void renderLevel(CallbackInfo ci) {
      if (!Raytracer.isDisabled()) {
         Raytracer.INSTANCE.getMainRenderer().setup();
      }
   }
}
