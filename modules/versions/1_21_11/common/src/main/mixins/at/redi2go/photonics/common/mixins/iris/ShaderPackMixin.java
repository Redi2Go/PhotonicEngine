package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.api.shaders.LightingMode;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.common.iris.PatcherBridge;
import at.redi2go.photonics.common.iris.ShaderPropertiesBridge;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.iris.patching.ShaderPatcher;
import at.redi2go.photonics.common.PhotonicsPropertiesImpl;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Mixin(ShaderPack.class)
public abstract class ShaderPackMixin implements IShaderPack {
    @Unique
    private PhotonicsPropertiesImpl phProperties;
    @Unique
    private ShaderPatcher patcher;

    @Unique
    private boolean supportsPhotonics = false;

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/EnumMap;<init>(Ljava/lang/Class;)V"
            )
    )
    private void init(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<String> environmentDefines,
            boolean isZip,
            CallbackInfo ci
    ) {
        phProperties = new PhotonicsPropertiesImpl();
        var properties = loadShaderProperties(root);

        supportsPhotonics = properties.containsKey(PhotonicsProperties.ENABLED_KEY);
        patcher = new ShaderPatcher(this);
        PatcherBridge.PATCHER = patcher;

        if (!supportsPhotonics && patcher.hasPatch())
            phProperties.enabled = Boolean.parseBoolean(
                    changedConfigs.getOrDefault("PHOTONICS_ENABLED", "true")
            );

        ShaderPropertiesBridge.PROPERTIES = phProperties;
    }

    @Override
    public String name() {
        return Iris.getIrisConfig()
                .getShaderPackName()
                .orElse("<unknown>");
    }

    @Override
    public boolean supportsPhotonics() {
        return supportsPhotonics;
    }

    @Override
    public PhotonicsProperties properties() {
        return phProperties;
    }

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
                    ordinal = 0
            )
    )
    private void addEnvironmentalDefines(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<StringPair> environmentDefines,
            boolean isZip,
            CallbackInfo ci,
            @Local(name = "envDefines1") ArrayList<StringPair> defines
    ) {
        // These are the defines used by the preprocessor for shaders.properties
        defines.add(new StringPair("PHOTONICS", ""));
        defines.add(new StringPair("PHOTONICS_VERSION", Photonics.getVersionString()));
    }

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
                    ordinal = 1
            )
    )
    private void addRegularDefines(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<StringPair> environmentDefines,
            boolean isZip,
            CallbackInfo ci,
            @Local(name = "newEnvDefines") List<StringPair> newEnvDefines
    ) {
        // These are the defines used by the preprocessor for everything else
        floatDefine(newEnvDefines, "PH_RENDER_SCALE", phProperties.getRenderScale());
        intDefine(newEnvDefines, "PH_MAX_LIGHTS", phProperties.getMaxLights());

        switch (phProperties.getAlphaMode()) {
            case BLOCK -> stringDefine(newEnvDefines, "PH_USE_TRANSPARENCY", "");
            case VOXEL -> {
                stringDefine(newEnvDefines, "PH_USE_TRANSPARENCY", "");
                stringDefine(newEnvDefines, "PH_FULL_TRANSPARENCY", "");
            }
        }

        if (phProperties.isGiEnabled())
            stringDefine(newEnvDefines, "PH_ENABLE_GI", "");

        if (phProperties.isBlockLightEnabled())
            stringDefine(newEnvDefines, "PH_ENABLE_BLOCKLIGHT", "");

        if (phProperties.isHandheldLightEnabled())
            stringDefine(newEnvDefines, "PH_ENABLE_HANDHELD_LIGHT", "");

        if (phProperties.isLightBinningEnabled() || phProperties.getLightingMode() == LightingMode.BASIC)
            stringDefine(newEnvDefines, "PH_ENABLE_LIGHT_BINNING", "");

        if (phProperties.useSeparateHandheldRays())
            stringDefine(newEnvDefines, "PH_SEPARATE_HANDHELD_RAYS", "");

        enumDefine(newEnvDefines, "PH_LIGHTING_MODE", phProperties.getLightingMode());

        intDefine(newEnvDefines, "PH_RESTIR_INITIAL_SAMPLES", phProperties.getRestirInitialSamples());
        intDefine(newEnvDefines, "PH_RESTIR_SPATIAL_REUSE_SAMPLES", phProperties.getRestirSpatialReuseSamples());
        floatDefine(newEnvDefines, "PH_RESTIR_SPATIAL_REUSE_RADIUS", phProperties.getRestirSpatialReuseRadius());
        intDefine(newEnvDefines, "PH_RESTIR_ACCUMULATION_FRAMES", phProperties.getRestirAccumulationFrames());
        intDefine(newEnvDefines, "PH_RESTIR_DENOISER_PASSES", phProperties.getRestirDenoiserPasses());

        if (phProperties.useRestirSoftShadows())
            stringDefine(newEnvDefines, "PH_RESTIR_SOFT_SHADOWS", "");

        if (phProperties.getLightingMode() == LightingMode.RESTIR && phProperties.useRestirCombinedGi())
            stringDefine(newEnvDefines, "PH_RESTIR_COMBINED_GI", "");

        intDefine(newEnvDefines, "PH_MAX_SAMPLES",  phProperties.getMaxSamples());
    }

    @Unique
    private static boolean isPhotonicsSource = false;

    @Inject(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;",
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private static void dimensionDefinesPre(List disabledPrograms, IncludeProcessor includeProcessor, Iterable finalEnvironmentDefines1, AbsolutePackPath path, CallbackInfoReturnable<String> cir) {
        isPhotonicsSource = path.getPathString().contains("photonics");
    }

    @ModifyArgs(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"
            ),
            remap = false
    )
    private static void dimensionDefines(
            Args args
    ) {
        if (!isPhotonicsSource)
            return;

        Iterable<StringPair> environmentDefines = args.get(1);

        List<StringPair> definitions = Lists.newArrayList(environmentDefines.iterator());

        String dimensionDefine = switch (Iris.getCurrentDimension().getName()) {
            case "the_nether" -> "NETHER";
            case "the_end" -> "END";

            default -> "OVERWORLD";
        };

        stringDefine(definitions, dimensionDefine, "");

        definitions.add(new StringPair(dimensionDefine, ""));

        args.set(1, definitions);
    }


    @Unique
    private static Properties loadShaderProperties(Path shaderPath) {
        var properties = new Properties();

        try (var reader = Files.newBufferedReader(shaderPath.resolve("shaders.properties"))) {
            properties.load(reader);
        } catch (IOException e) {
            // Ignored
        }

        return properties;
    }

    @Unique
    private static void stringDefine(List<StringPair> defines, String name, String value) {
        defines.add(new StringPair(name, value));
    }

    @Unique
    private static void intDefine(List<StringPair> defines, String name, int value) {
        defines.add(new StringPair(name, Integer.toString(value)));
    }

    @Unique
    private static void floatDefine(List<StringPair> defines, String name, float value) {
        defines.add(new StringPair(name, Float.toString(value)));
    }

    @Unique
    private static <T extends Enum<T>> void enumDefine(List<StringPair> defines, String name, T value) {
        defines.add(new StringPair(name, Integer.toString(value.ordinal())));
    }
}