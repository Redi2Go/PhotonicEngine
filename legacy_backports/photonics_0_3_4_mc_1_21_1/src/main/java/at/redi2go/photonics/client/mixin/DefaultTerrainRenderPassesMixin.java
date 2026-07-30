package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import java.util.ArrayList;
import java.util.List;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {
   @Mutable
   @Shadow(remap = false)
   @Final
   public static TerrainRenderPass[] ALL;

   @Inject(method = "<clinit>", at = @At("TAIL"))
   private static void clinit(CallbackInfo ci) {
      ArrayList<TerrainRenderPass> passes = new ArrayList<>(List.of(ALL));
      passes.add(Raytracer.VOXEL);
      ALL = passes.toArray(new TerrainRenderPass[0]);
   }
}
