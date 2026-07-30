package at.redi2go.photonics.client.rendering.opengl.rendering.renderers;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.api.PhotonicsProperties;
import at.redi2go.photonics.client.rendering.opengl.rendering.ColorFramebuffer;
import at.redi2go.photonics.client.rendering.opengl.rendering.PhotonicsShader;
import at.redi2go.photonics.client.rendering.world.WorldRegistry;
import java.util.function.Function;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import org.jetbrains.annotations.Nullable;

public class RestirRenderer extends MainRenderer {
   private final ColorFramebuffer geometryBuffer;
   private final ColorFramebuffer reuseSamplesBuffer;
   private final ColorFramebuffer[] spatialReuseBuffers;
   private final ColorFramebuffer lightingBuffer;
   private final ColorFramebuffer historyBuffer;
   @Nullable
   private CompositeRenderer lightingRenderer;
   @Nullable
   private final ColorFramebuffer denoisingBuffer;
   @Nullable
   private CompositeRenderer denoisingRenderer;
   private int atrousIteration = 0;
   private int lastRenderedBuildTime = Integer.MIN_VALUE;
   private boolean sceneChanged = true;
   private final int denoiserPasses;
   private final boolean isGiPassDisabled;

   public RestirRenderer(WorldRegistry worldRegistry, float renderScale, PhotonicsProperties properties) {
      super(worldRegistry, renderScale);
      this.isGiPassDisabled = properties.useRestirCombinedGi().orElse(false) || !properties.isGiEnabled().orElse(true);
      int requestedDenoiserPasses = properties.getRestirDenoiserPasses();
      this.denoiserPasses = requestedDenoiserPasses;

      Photonics.info(
         "ReSTIR diagnostics: denoiserPassesRequested={}, denoiserPassesEffective={}, denoiserDrawsPerFrame={}, combinedGi={}, giPassDisabled={}",
         requestedDenoiserPasses,
         this.denoiserPasses,
         this.denoiserPasses == 0 ? 0 : this.denoiserPasses + 1,
         properties.useRestirCombinedGi().orElse(false),
         this.isGiPassDisabled
      );

      this.geometryBuffer = new ColorFramebuffer(renderScale);
      this.geometryBuffer.createAttachment("position", "RGB32F", false);
      this.geometryBuffer.createAttachment("normal", "RGBA16F", false);
      this.geometryBuffer.createAttachment("reservoirs_initial", "RGBA32F", false);

      this.reuseSamplesBuffer = new ColorFramebuffer(renderScale);
      this.reuseSamplesBuffer.createAttachment("samples", "R16F", false);

      this.spatialReuseBuffers = new ColorFramebuffer[3];
      for (int i = 0; i < this.spatialReuseBuffers.length; i++) {
         this.spatialReuseBuffers[i] = new ColorFramebuffer(renderScale);
         this.spatialReuseBuffers[i].createAttachment("reservoirs", "RGBA32F", false);
      }

      this.lightingBuffer = new ColorFramebuffer(renderScale);
      this.lightingBuffer.createAttachment("reservoirs", "RGBA32F", false);
      this.lightingBuffer.createAttachment("lighting_raw", "RGBA32F", false);
      this.lightingBuffer.createAttachment("handheld", "RGBA16F", false);

      this.historyBuffer = new ColorFramebuffer(renderScale);
      this.historyBuffer.createAttachment("lighting", "RGBA32F", false);
      this.historyBuffer.createAttachment("lighting_variance", "RGBA32F", false);

      if (this.denoiserPasses != 0) {
         this.denoisingBuffer = new ColorFramebuffer(renderScale);
         this.denoisingBuffer.createAttachment("color", "RGB16F", true);
         this.denoisingBuffer.createAttachment("variance", "R16F", true);
      } else {
         this.denoisingBuffer = null;
      }
   }

   @Override
   public void createCompositeRenderer(Function<@Nullable PhotonicsShader[], CompositeRenderer> rendererCreator) {
      this.lightingRenderer = rendererCreator.apply(
         new PhotonicsShader[]{
            new PhotonicsShader("restir/sampling.fsh", "common/screen.vsh", this.memoryCollection, this.geometryBuffer),
            new PhotonicsShader("restir/spatial_reuse/setup.fsh", "common/screen.vsh", this.memoryCollection, this.reuseSamplesBuffer),
            new PhotonicsShader("restir/spatial_reuse/pass0.fsh", "common/screen.vsh", this.memoryCollection, this.spatialReuseBuffers[0]),
            new PhotonicsShader("restir/spatial_reuse/pass1.fsh", "common/screen.vsh", this.memoryCollection, this.spatialReuseBuffers[1]),
            new PhotonicsShader("restir/spatial_reuse/pass2.fsh", "common/screen.vsh", this.memoryCollection, this.spatialReuseBuffers[2]),
            new PhotonicsShader("restir/lighting.fsh", "common/screen.vsh", this.memoryCollection, this.lightingBuffer),
            new PhotonicsShader("restir/accumulation.fsh", "common/screen.vsh", this.memoryCollection, this.historyBuffer),
            this.isGiPassDisabled ? null : new PhotonicsShader("common/indirect.fsh", "common/screen.vsh", this.memoryCollection, null)
         }
      );
      if (this.denoiserPasses != 0) {
         this.denoisingRenderer = rendererCreator.apply(
            new PhotonicsShader[]{new PhotonicsShader("restir/denoising.fsh", "common/screen.vsh", this.memoryCollection, this.denoisingBuffer)}
         );
      }
   }

   @Override
   public void registerCustomTextures(SamplerHolder samplers) {
      this.addTextureSampler(samplers, "radiosity_position", () -> this.geometryBuffer.getWriteAttachment("position"));
      this.addTextureSampler(samplers, "radiosity_normal", () -> this.geometryBuffer.getWriteAttachment("normal"));
      this.addTextureSampler(samplers, "radiosity_reservoirs_initial", () -> this.geometryBuffer.getWriteAttachment("reservoirs_initial"));
      this.addTextureSampler(samplers, "radiosity_reservoirs_spatial_0", () -> this.spatialReuseBuffers[0].getWriteAttachment("reservoirs"));
      this.addTextureSampler(samplers, "radiosity_reservoirs_spatial_1", () -> this.spatialReuseBuffers[1].getWriteAttachment("reservoirs"));
      this.addTextureSampler(samplers, "radiosity_reservoirs_spatial_2", () -> this.spatialReuseBuffers[2].getWriteAttachment("reservoirs"));
      this.addTextureSampler(samplers, "radiosity_reservoirs", () -> this.lightingBuffer.getWriteAttachment("reservoirs"));
      this.addTextureSampler(samplers, "radiosity_lighting_raw", () -> this.lightingBuffer.getWriteAttachment("lighting_raw"));
      this.addTextureSampler(samplers, "radiosity_lighting", () -> this.historyBuffer.getWriteAttachment("lighting"));
      this.addTextureSampler(samplers, "radiosity_lighting_variance", () -> this.historyBuffer.getWriteAttachment("lighting_variance"));
      this.addTextureSampler(samplers, "radiosity_lighting_samples", () -> this.reuseSamplesBuffer.getWriteAttachment("samples"));
      this.addTextureSampler(samplers, "radiosity_handheld", () -> this.lightingBuffer.getWriteAttachment("handheld"));
      this.addTextureSampler(samplers, "prev_radiosity_position", () -> this.geometryBuffer.getReadAttachment("position"));
      this.addTextureSampler(samplers, "prev_radiosity_normal", () -> this.geometryBuffer.getReadAttachment("normal"));
      this.addTextureSampler(samplers, "prev_radiosity_reservoirs", () -> this.lightingBuffer.getReadAttachment("reservoirs"));
      this.addTextureSampler(samplers, "prev_radiosity_lighting", () -> this.historyBuffer.getReadAttachment("lighting"));
      this.addTextureSampler(samplers, "prev_radiosity_lighting_variance", () -> this.historyBuffer.getReadAttachment("lighting_variance"));
      this.addTextureSampler(samplers, "prev_radiosity_lighting_samples", () -> this.reuseSamplesBuffer.getReadAttachment("samples"));
      this.addTextureSampler(samplers, "prev_radiosity_handheld", () -> this.lightingBuffer.getReadAttachment("handheld"));
      if (this.denoisingBuffer != null) {
         this.addTextureSampler(samplers, "denoise_color", () -> this.denoisingBuffer.getWriteAttachment("color"));
         this.addTextureSampler(samplers, "denoise_variance", () -> this.denoisingBuffer.getWriteAttachment("variance"));
         this.addTextureSampler(samplers, "prev_denoise_color", () -> this.denoisingBuffer.getReadAttachment("color"));
         this.addTextureSampler(samplers, "prev_denoise_variance", () -> this.denoisingBuffer.getReadAttachment("variance"));
      }
   }

   @Override
   public void registerCustomUniforms(DynamicUniformHolder uniforms) {
      uniforms.uniform1i("ph_scene_changed", () -> this.sceneChanged ? 1 : 0, updater -> {
         if (updater != null) {
            updater.run();
         }
      });

      if (this.denoiserPasses != 0) {
         uniforms.uniform1i("atrous_iteration", () -> this.atrousIteration, updater -> {
            if (updater != null) {
               updater.run();
            }
         });
      }
   }

   @Override
   public void render() {
      if (this.lightingRenderer != null) {
         int currentBuildTime = this.worldRegistry.getLastBuildTime();
         this.sceneChanged = currentBuildTime != this.lastRenderedBuildTime;
         this.lastRenderedBuildTime = currentBuildTime;
         this.geometryBuffer.swap();
         this.lightingBuffer.swap();
         this.historyBuffer.swap();
         this.lightingRenderer.renderAll();
         if (this.denoisingRenderer != null && this.denoisingBuffer != null) {
            for (this.atrousIteration = -1; this.atrousIteration < this.denoiserPasses; this.atrousIteration++) {
               this.denoisingBuffer.swap();
               this.denoisingRenderer.renderAll();
            }
         }
      }
   }

   @Override
   public void recalculateSizes() {
      if (this.lightingRenderer != null && this.denoisingRenderer != null) {
         this.lightingRenderer.recalculateSizes();
         this.denoisingRenderer.recalculateSizes();
      }
   }

   @Override
   public void free() {
      this.geometryBuffer.destroy();
      this.reuseSamplesBuffer.destroy();
      for (ColorFramebuffer spatialReuseBuffer : this.spatialReuseBuffers) {
         spatialReuseBuffer.destroy();
      }

      this.lightingBuffer.destroy();
      this.historyBuffer.destroy();
      if (this.lightingRenderer != null) {
         this.lightingRenderer.destroy();
      }

      if (this.denoisingBuffer != null) {
         this.denoisingBuffer.destroy();
      }

      if (this.denoisingRenderer != null) {
         this.denoisingRenderer.destroy();
      }
   }
}
