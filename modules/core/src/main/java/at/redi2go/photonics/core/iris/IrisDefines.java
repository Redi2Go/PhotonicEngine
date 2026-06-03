package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.api.shaders.LightingMode;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.pipeline.DefineHolder;

public class IrisDefines {
    public static void registerVersionDefines(DefineHolder defines) {
        defines.stringDefine("PHOTONICS", "");
        defines.stringDefine("PHOTONICS_VERSION", Photonics.getVersionString());
    }

    public static void registerDefines(DefineHolder defines, PhotonicsProperties phProperties) {
        defines.floatDefine("PH_RENDER_SCALE", phProperties.getRenderScale());
        defines.intDefine("PH_MAX_LIGHTS", phProperties.getMaxLights());

        switch (phProperties.getAlphaMode()) {
            case BLOCK -> defines.stringDefine("PH_USE_TRANSPARENCY", "");
            case VOXEL -> {
                defines.stringDefine("PH_USE_TRANSPARENCY", "");
                defines.stringDefine("PH_FULL_TRANSPARENCY", "");
            }
        }

        if (phProperties.isGiEnabled())
            defines.stringDefine("PH_ENABLE_GI", "");

        if (phProperties.isBlockLightEnabled())
            defines.stringDefine("PH_ENABLE_BLOCKLIGHT", "");

        if (phProperties.isHandheldLightEnabled())
            defines.stringDefine("PH_ENABLE_HANDHELD_LIGHT", "");

        if (phProperties.isLightBinningEnabled() || phProperties.getLightingMode() == LightingMode.BASIC)
            defines.stringDefine("PH_ENABLE_LIGHT_BINNING", "");

        if (phProperties.useSeparateHandheldRays())
            defines.stringDefine("PH_SEPARATE_HANDHELD_RAYS", "");

        defines.enumDefine("PH_LIGHTING_MODE", phProperties.getLightingMode());

        defines.intDefine("PH_RESTIR_INITIAL_SAMPLES", phProperties.getRestirInitialSamples());
        defines.intDefine("PH_RESTIR_SPATIAL_REUSE_SAMPLES", phProperties.getRestirSpatialReuseSamples());
        defines.floatDefine("PH_RESTIR_SPATIAL_REUSE_RADIUS", phProperties.getRestirSpatialReuseRadius());
        defines.intDefine("PH_RESTIR_ACCUMULATION_FRAMES", phProperties.getRestirAccumulationFrames());
        defines.intDefine("PH_RESTIR_DENOISER_PASSES", phProperties.getRestirDenoiserPasses());

        if (phProperties.useRestirSoftShadows())
            defines.stringDefine("PH_RESTIR_SOFT_SHADOWS", "");

        if (phProperties.getLightingMode() == LightingMode.RESTIR && phProperties.useRestirCombinedGi())
            defines.stringDefine("PH_RESTIR_COMBINED_GI", "");

        defines.intDefine("PH_MAX_SAMPLES",  phProperties.getMaxSamples());
    }
}
