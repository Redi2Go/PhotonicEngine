package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.CompositeRendererExt;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.rendering.opengl.rendering.PhotonicsShader;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pipeline.CompositePass;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.CustomTextureManager;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderMap;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IrisRenderingPipeline.class, remap = false)
public abstract class IrisRenderingPipelineMixin {
   @Shadow
   @Final
   private RenderTargets renderTargets;
   @Shadow
   private ShaderStorageBufferHolder shaderStorageBufferHolder;
   @Shadow
   @Final
   private CustomTextureManager customTextureManager;
   @Shadow
   @Final
   private FrameUpdateNotifier updateNotifier;
   @Shadow
   @Final
   private CenterDepthSampler centerDepthSampler;
   @Shadow
   @Final
   private Supplier<ShadowRenderTargets> shadowTargetsSupplier;
   @Shadow
   @Final
   private Set<GlImage> customImages;
   @Shadow
   @Final
   private CustomUniforms customUniforms;
   @Shadow
   @Final
   private ShaderMap shaderMap;

   @Unique
   private static String readSource(String fileName) {
      ShaderPack shaderPack = (ShaderPack)Iris.getCurrentPack().orElse(null);
      if (shaderPack == null) {
         return null;
      }

      AbsolutePackPath path = AbsolutePackPath.fromAbsolutePath("/photonics/" + fileName);
      return ((ShaderPackAccessor)shaderPack).getSourceProvider().apply(path);
   }

   @Inject(
      method = "<init>",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;<init>(Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;Lnet/irisshaders/iris/pipeline/CompositePass;Lnet/irisshaders/iris/shaderpack/properties/PackDirectives;[Lnet/irisshaders/iris/shaderpack/programs/ProgramSource;[[Lnet/irisshaders/iris/shaderpack/programs/ComputeSource;Lnet/irisshaders/iris/targets/RenderTargets;Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;Lnet/irisshaders/iris/gl/texture/TextureAccess;Lnet/irisshaders/iris/uniforms/FrameUpdateNotifier;Lnet/irisshaders/iris/pathways/CenterDepthSampler;Lnet/irisshaders/iris/targets/BufferFlipper;Ljava/util/function/Supplier;Lnet/irisshaders/iris/shaderpack/texture/TextureStage;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Ljava/util/Set;Lcom/google/common/collect/ImmutableMap;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)V",
         ordinal = 0
      )
   )
   public void init(ProgramSet programSet, CallbackInfo ci, @Local BufferFlipper flipper) {
      if (Raytracer.shouldBeEnabled()) {
         if (Raytracer.INSTANCE != null) {
            Raytracer.INSTANCE.free();
         }

         Raytracer.INSTANCE = new Raytracer();
         Raytracer.INSTANCE.getWorldRegistry().getLightRegistry().init();
         Raytracer.INSTANCE
            .getMainRenderer()
            .createCompositeRenderer(
               shaders -> {
                  List<PhotonicsShader> photonicsShaders = new ArrayList<>(Arrays.asList(shaders));
                  ProgramSource[] compositeSources = new ProgramSource[shaders.length];

                  for (int i = 0; i < shaders.length; i++) {
                     PhotonicsShader shader = shaders[i];
                     if (shader != null) {
                        compositeSources[i] = new ProgramSource(
                           shader.getFragmentName().substring(shader.getFragmentName().lastIndexOf(47) + 1),
                           readSource(shader.getVertexName()),
                           null,
                           null,
                           null,
                           readSource(shader.getFragmentName()),
                           programSet,
                           null,
                           null
                        );
                     }
                  }

                  CompositeRenderer compositeRenderer = new CompositeRenderer(
                     (IrisRenderingPipeline)(Object)this,
                     CompositePass.DEFERRED,
                     programSet.getPackDirectives(),
                     compositeSources,
                     new ComputeSource[0][0],
                     this.renderTargets,
                     this.shaderStorageBufferHolder,
                     this.customTextureManager.getNoiseTexture(),
                     this.updateNotifier,
                     this.centerDepthSampler,
                     flipper,
                     this.shadowTargetsSupplier,
                     TextureStage.DEFERRED,
                     this.customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
                     this.customTextureManager.getIrisCustomTextures(),
                     this.customImages,
                     programSet.getPackDirectives().getExplicitFlips("photonics"),
                     this.customUniforms
                  );
                  ((CompositeRendererExt)compositeRenderer).photonics$setPhotonicsShaders(photonicsShaders);
                  return compositeRenderer;
               }
            );
      }
   }

   @Inject(
      method = "beginLevelRendering",
      at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;recalculateSizes()V", ordinal = 0)
   )
   public void recalculateSizes0(CallbackInfo ci) {
      if (!Raytracer.isDisabled()) {
         Raytracer.INSTANCE.getMainRenderer().recalculateSizes();
      }
   }

   @Inject(method = "destroy", at = @At("HEAD"))
   public void destroy(CallbackInfo ci) {
      if (Raytracer.INSTANCE != null) {
         Raytracer.INSTANCE.free();
         Raytracer.INSTANCE = null;
      }
   }

   @Inject(method = "beginTranslucents", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;renderAll()V"))
   public void beginTranslucents(CallbackInfo ci) {
      if (!Raytracer.isDisabled()) {
         Raytracer.INSTANCE.getMainRenderer().render();
      }
   }
}
