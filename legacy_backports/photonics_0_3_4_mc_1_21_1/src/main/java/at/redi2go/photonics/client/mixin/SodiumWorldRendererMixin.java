package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class SodiumWorldRendererMixin {
   @Shadow(remap = false)
   private RenderSectionManager renderSectionManager;

   @Inject(method = "drawChunkLayer", at = @At("HEAD"))
   public void drawChunkLayer(RenderType renderType, ChunkRenderMatrices matrices, double x, double y, double z, CallbackInfo ci) {
      if (renderType == RenderType.solid()) {
         this.renderSectionManager.renderLayer(matrices, Raytracer.VOXEL, x, y, z);
      }
   }
}
