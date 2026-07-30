package at.redi2go.photonics.client.api;

import net.irisshaders.iris.helpers.OptionalBoolean;

public interface PhotonicsProperties {
   float DEFAULT_RENDER_SCALE = 1.0F;
   int DEFAULT_MAX_LIGHTS = 1000;
   AlphaMode DEFAULT_ALPHA_MODE = AlphaMode.NONE;
   float DEFAULT_ENCHANTMENT_GLINT_STRENGTH = 0.2F;
   LightingMode DEFAULT_LIGHTING_MODE = LightingMode.BASIC;
   int DEFAULT_RESTIR_INITIAL_SAMPLES = 32;
   int DEFAULT_RESTIR_SPATIAL_REUSE_SAMPLES = 5;
   float DEFAULT_RESTIR_SPATIAL_REUSE_RADIUS = 10.0F;
   int DEFAULT_RESTIR_ACCUMULATION_FRAMES = 32;
   int DEFAULT_RESTIR_DENOISER_PASSES = 5;
   int DEFAULT_MAX_SAMPLES = 20;

   OptionalBoolean isPhotonicsEnabled();

   float getRenderScale();

   int getMaxLights();

   AlphaMode getAlphaMode();

   float getEnchantmentGlintStrength();

   OptionalBoolean useSeparateHandheldRays();

   OptionalBoolean isGiEnabled();

   OptionalBoolean isBlockLightEnabled();

   OptionalBoolean isHandheldLightEnabled();

   OptionalBoolean isLightBinningEnabled();

   OptionalBoolean voxelizeLava();

   LightingMode getLightingMode();

   int getRestirInitialSamples();

   int getRestirSpatialReuseSamples();

   float getRestirSpatialReuseRadius();

   int getRestirAccumulationFrames();

   OptionalBoolean useRestirSoftShadows();

   OptionalBoolean useRestirCombinedGi();

   int getRestirDenoiserPasses();

   int getMaxSamples();
}
