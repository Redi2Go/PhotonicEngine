package at.redi2go.photonics.common;

import at.redi2go.photonics.api.shaders.AlphaMode;
import at.redi2go.photonics.api.shaders.LightingMode;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;

public class PhotonicsPropertiesImpl implements PhotonicsProperties {
    public boolean enabled = PhotonicsProperties.DEFAULT_ENABLED;
    public float renderScale = PhotonicsProperties.DEFAULT_RENDER_SCALE;
    public int maxLights = PhotonicsProperties.DEFAULT_MAX_LIGHTS;
    public AlphaMode alphaMode = PhotonicsProperties.DEFAULT_ALPHA_MODE;
    public float enchantmentGlintStrength = PhotonicsProperties.DEFAULT_ENCHANTMENT_GLINT_STRENGTH;
    public boolean useSeparateHandheldRays = PhotonicsProperties.DEFAULT_SEPARATE_HANDHELD_RAYS;
    public boolean giEnabled = PhotonicsProperties.DEFAULT_IS_GI_ENABLED;
    public boolean blockLightEnabled = PhotonicsProperties.DEFAULT_IS_BLOCK_LIGHT_ENABLED;
    public boolean handheldLightEnabled = PhotonicsProperties.DEFAULT_IS_HANDHELD_LIGHT_ENABLED;
    public boolean lightBinningEnabled = PhotonicsProperties.DEFAULT_IS_LIGHT_BINNING_ENABLED;
    public LightingMode lightingMode = PhotonicsProperties.DEFAULT_LIGHTING_MODE;
    public int restirInitialSamples = PhotonicsProperties.DEFAULT_RESTIR_INITIAL_SAMPLES;
    public int restirSpatialReuseSamples = PhotonicsProperties.DEFAULT_RESTIR_SPATIAL_REUSE_SAMPLES;
    public float restirRestirSpatialReuseRadius = PhotonicsProperties.DEFAULT_RESTIR_SPATIAL_REUSE_RADIUS;
    public int restirAccumulationFrames = PhotonicsProperties.DEFAULT_RESTIR_ACCUMULATION_FRAMES;
    public boolean restirSoftShadows = PhotonicsProperties.DEFAULT_USE_RESTIR_SOFT_SHADOWS;
    public boolean restirCombinedGi = PhotonicsProperties.DEFAULT_USE_RESTIR_COMBINED_GI;
    public int restirDenoiserPasses = PhotonicsProperties.DEFAULT_RESTIR_DENOISER_PASSES;
    public int maxSamples = PhotonicsProperties.DEFAULT_MAX_SAMPLES;

    @Override
    public boolean isPhotonicsEnabled() {
        return enabled;
    }

    @Override
    public float getRenderScale() {
        return renderScale;
    }

    @Override
    public int getMaxLights() {
        return maxLights;
    }

    @Override
    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    @Override
    public float getEnchantmentGlintStrength() {
        return enchantmentGlintStrength;
    }

    @Override
    public boolean useSeparateHandheldRays() {
        return useSeparateHandheldRays;
    }

    @Override
    public boolean isGiEnabled() {
        return giEnabled;
    }

    @Override
    public boolean isBlockLightEnabled() {
        return blockLightEnabled;
    }

    @Override
    public boolean isHandheldLightEnabled() {
        return handheldLightEnabled;
    }

    @Override
    public LightingMode getLightingMode() {
        return lightingMode;
    }

    @Override
    public boolean isLightBinningEnabled() {
        return lightBinningEnabled;
    }

    @Override
    public int getMaxSamples() {
        return maxSamples;
    }

    @Override
    public int getRestirInitialSamples() {
        return restirInitialSamples;
    }

    @Override
    public int getRestirSpatialReuseSamples() {
        return restirSpatialReuseSamples;
    }

    @Override
    public float getRestirSpatialReuseRadius() {
        return restirRestirSpatialReuseRadius;
    }

    @Override
    public int getRestirAccumulationFrames() {
        return restirAccumulationFrames;
    }

    @Override
    public boolean useRestirSoftShadows() {
        return restirSoftShadows;
    }

    @Override
    public boolean useRestirCombinedGi() {
        return restirCombinedGi;
    }

    @Override
    public int getRestirDenoiserPasses() {
        return restirDenoiserPasses;
    }
}
