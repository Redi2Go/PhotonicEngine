package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import com.google.common.collect.ImmutableSet;
import java.util.function.Supplier;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms.Pass;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.targets.RenderTargets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SodiumPrograms.class, remap = false)
public class SodiumProgramsMixin {
   @Inject(method = "mapTerrainRenderPass", at = @At("HEAD"), cancellable = true)
   public void mapTerrainRenderPass(TerrainRenderPass pass, CallbackInfoReturnable<Pass> cir) {
      if (pass == Raytracer.VOXEL) {
         Pass sodiumPass = ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.valueOf("SHADOW_VOXELS") : Pass.valueOf("VOXELS");
         cir.setReturnValue(sodiumPass);
      }
   }

   @Inject(method = "createFramebuffer", at = @At("HEAD"), cancellable = true)
   public void createFramebuffer(
      Pass pass,
      ProgramSource source,
      Supplier<ShadowRenderTargets> shadowRenderTargets,
      RenderTargets renderTargets,
      Supplier<ImmutableSet<Integer>> flipState,
      CallbackInfoReturnable<GlFramebuffer> cir
   ) {
      if (pass == Pass.valueOf("SHADOW_VOXELS")) {
         GlFramebuffer framebuffer = shadowRenderTargets.get()
            .createShadowFramebuffer(
               ImmutableSet.of(),
               source == null ? new int[]{0, 1} : (source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers())
            );
         cir.setReturnValue(framebuffer);
         cir.cancel();
      }
   }
}
