package at.redi2go.photonics.client.mixin;

import java.util.List;
import java.util.function.Supplier;
import net.caffeinemc.mods.sodium.client.render.chunk.shader.ShaderBindingContext;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.SodiumShader;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms.Pass;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumShader.class, remap = false)
public class SodiumShaderMixin {
   private static final String CONSTRUCTOR = "<init>(Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;Lnet/irisshaders/iris/pipeline/programs/SodiumPrograms$Pass;Lnet/caffeinemc/mods/sodium/client/render/chunk/shader/ShaderBindingContext;ILnet/irisshaders/iris/gl/blending/BlendModeOverride;Ljava/util/List;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;Ljava/util/function/Supplier;FZ)V";

   @Unique
   private boolean photonics$isShadowVoxelPass;

   @Inject(
      method = CONSTRUCTOR,
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/pipeline/programs/SodiumShader;buildUniforms(Lnet/irisshaders/iris/pipeline/programs/SodiumPrograms$Pass;ILnet/irisshaders/iris/uniforms/custom/CustomUniforms;)Lnet/irisshaders/iris/gl/program/ProgramUniforms;"
      )
   )
   private void captureShadowVoxelPass(
      IrisRenderingPipeline pipeline,
      Pass pass,
      ShaderBindingContext context,
      int handle,
      BlendModeOverride blendModeOverride,
      List bufferBlendOverrides,
      CustomUniforms customUniforms,
      Supplier flipState,
      float alphaTest,
      boolean containsTessellation,
      CallbackInfo ci
   ) {
      this.photonics$isShadowVoxelPass = pass == Pass.valueOf("SHADOW_VOXELS");
   }

   @ModifyArg(
      method = CONSTRUCTOR,
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/pipeline/programs/SodiumShader;buildSamplers(Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;Lnet/irisshaders/iris/pipeline/programs/SodiumPrograms$Pass;IZLjava/util/function/Supplier;)Lnet/irisshaders/iris/gl/program/ProgramSamplers;"
      ),
      index = 3
   )
   private boolean includeShadowVoxelSamplers(boolean isShadowPass) {
      return isShadowPass || this.photonics$isShadowVoxelPass;
   }

   @ModifyArg(
      method = CONSTRUCTOR,
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/pipeline/programs/SodiumShader;buildImages(Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;Lnet/irisshaders/iris/pipeline/programs/SodiumPrograms$Pass;IZLjava/util/function/Supplier;)Lnet/irisshaders/iris/gl/program/ProgramImages;"
      ),
      index = 3
   )
   private boolean includeShadowVoxelImages(boolean isShadowPass) {
      return isShadowPass || this.photonics$isShadowVoxelPass;
   }
}
