package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.ShaderPackPath;
import at.redi2go.photonics.client.UniformPatcher;
import at.redi2go.photonics.client.api.LightingMode;
import at.redi2go.photonics.client.api.PhotonicsProperties;
import at.redi2go.photonics.client.rendering.patching.Patch;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import net.fabricmc.loader.api.SemanticVersion;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import net.irisshaders.iris.shaderpack.option.OrderBackedProperties;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ShaderPack.class, remap = false)
public abstract class ShaderPackMixin {
   @Shadow
   @Final
   private ShaderProperties shaderProperties;
   @Unique
   private static boolean isPhotonicsSource = false;
   @Unique
   private static boolean patchProperties = true;

   @Shadow
   private static Optional<String> loadProperties(Path shaderPath, String name) {
      return Optional.empty();
   }

   @Inject(
      method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
      at = @At(value = "INVOKE", target = "Ljava/util/EnumMap;<init>(Ljava/lang/Class;)V")
   )
   private void init(Path root, Map<String, String> changedConfigs, ImmutableList<StringPair> environmentDefines, boolean isZip, CallbackInfo ci) {
      Raytracer.reloadPatches();
      Raytracer.SHADERPACK_CHANGED_OPTIONS = changedConfigs;

      try {
         Raytracer.SHADERPACK_PROPERTIES = new OrderBackedProperties();
         patchProperties = false;
         Raytracer.SHADERPACK_PROPERTIES.load(new StringReader(loadProperties(root, "shaders.properties").orElseThrow()));
         patchProperties = true;
         Raytracer.PATCHED_SHADERPACK_PROPERTIES = new OrderBackedProperties();
         Raytracer.PATCHED_SHADERPACK_PROPERTIES.load(new StringReader(loadProperties(root, "shaders.properties").orElseThrow()));
      } catch (IOException e) {
         Raytracer.SHADERPACK_PROPERTIES = new Properties();
         Raytracer.PATCHED_SHADERPACK_PROPERTIES = new Properties();
         Photonics.error(e);
      }
   }

   @Inject(method = "readProperties", at = @At("HEAD"), cancellable = true)
   private static void readProperties(Path shaderPath, String name, CallbackInfoReturnable<String> cir) {
      if (patchProperties) {
         Patch patch = Raytracer.getAppliedPatch();
         if (patch == null) {
            return;
         }

         String patchedFile = patch.readPatchedFile(new ShaderPackPath(shaderPath.resolve(name)));
         if (patchedFile == null) {
            return;
         }

         cir.setReturnValue(patchedFile);
         cir.cancel();
      }
   }

   @Inject(
      method = "lambda$new$8",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;",
         shift = Shift.BEFORE
      ),
      remap = false
   )
   private static void lambda$new$8Pre(
      List disabledPrograms, IncludeProcessor includeProcessor, Iterable finalEnvironmentDefines1, AbsolutePackPath path, CallbackInfoReturnable<String> cir
   ) {
      isPhotonicsSource = path.getPathString().contains("photonics");
   }

   @WrapOperation(
      method = "lambda$new$8",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"
      ),
      remap = false
   )
   private static String lambda$new$8Post(String source, Iterable<StringPair> environmentDefines, Operation<String> original) {
      UniformPatcher.prepare();

      try {
         return UniformPatcher.addRequiredUniforms((String)original.call(new Object[]{source, environmentDefines}));
      } catch (CommandSyntaxException e) {
         throw new RuntimeException(e);
      }
   }

   @Inject(
      method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
      at = @At(
         value = "INVOKE",
         target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
         ordinal = 0
      )
   )
   private void addDefines0(
      Path root,
      Map<String, String> changedConfigs,
      ImmutableList<StringPair> environmentDefines,
      boolean isZip,
      CallbackInfo ci,
      @Local(name = "envDefines1") ArrayList<StringPair> envDefines1
   ) {
      envDefines1.add(new StringPair("PHOTONICS", ""));
      SemanticVersion version = (SemanticVersion)Photonics.VERSION;
      String major;
      if (version.getVersionComponentCount() >= 1) {
         major = Integer.toString(version.getVersionComponent(0));
      } else {
         major = "0";
      }

      String minor;
      if (version.getVersionComponentCount() >= 2) {
         minor = Integer.toString(version.getVersionComponent(1));
      } else {
         minor = "00";
      }

      String increment;
      if (version.getVersionComponentCount() >= 3) {
         increment = Integer.toString(version.getVersionComponent(2));
      } else {
         increment = "00";
      }

      envDefines1.add(
         new StringPair("PHOTONICS_VERSION", StringUtils.stripStart(major + StringUtils.leftPad(minor, 2, '0') + StringUtils.leftPad(increment, 2, '0'), "0"))
      );
   }

   @Inject(
      method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
      at = @At(
         value = "INVOKE",
         target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
         ordinal = 1
      )
   )
   private void addDefines2(
      Path root,
      Map<String, String> changedConfigs,
      ImmutableList<StringPair> environmentDefines,
      boolean isZip,
      CallbackInfo ci,
      @Local(name = "newEnvDefines") List<StringPair> newEnvDefines
   ) {
      PhotonicsProperties properties = (PhotonicsProperties)this.shaderProperties;
      floatDefine(newEnvDefines, "PH_RENDER_SCALE", properties.getRenderScale());
      intDefine(newEnvDefines, "PH_MAX_LIGHTS", properties.getMaxLights());
      properties.getAlphaMode().registerDefines(newEnvDefines);
      if (properties.isGiEnabled().orElse(true)) {
         stringDefine(newEnvDefines, "PH_ENABLE_GI", "");
      }

      if (properties.isBlockLightEnabled().orElse(true)) {
         stringDefine(newEnvDefines, "PH_ENABLE_BLOCKLIGHT", "");
      }

      if (properties.isHandheldLightEnabled().orElse(true)) {
         stringDefine(newEnvDefines, "PH_ENABLE_HANDHELD_LIGHT", "");
      }

      if (properties.isLightBinningEnabled().orElse(properties.getLightingMode() != LightingMode.OFF)) {
         stringDefine(newEnvDefines, "PH_ENABLE_LIGHT_BINNING", "");
      }

      if (properties.useSeparateHandheldRays().orElse(true)) {
         stringDefine(newEnvDefines, "PH_SEPARATE_HANDHELD_RAYS", "");
      }

      enumDefine(newEnvDefines, "PH_LIGHTING_MODE", properties.getLightingMode());
      intDefine(newEnvDefines, "PH_RESTIR_INITIAL_SAMPLES", properties.getRestirInitialSamples());
      intDefine(newEnvDefines, "PH_RESTIR_SPATIAL_REUSE_SAMPLES", properties.getRestirSpatialReuseSamples());
      floatDefine(newEnvDefines, "PH_RESTIR_SPATIAL_REUSE_RADIUS", properties.getRestirSpatialReuseRadius());
      intDefine(newEnvDefines, "PH_RESTIR_ACCUMULATION_FRAMES", properties.getRestirAccumulationFrames());
      intDefine(newEnvDefines, "PH_RESTIR_DENOISER_PASSES", properties.getRestirDenoiserPasses());
      if (properties.useRestirSoftShadows().orElse(true)) {
         stringDefine(newEnvDefines, "PH_RESTIR_SOFT_SHADOWS", "");
      }

      if (properties.getLightingMode() == LightingMode.RESTIR && properties.useRestirCombinedGi().orElse(false)) {
         stringDefine(newEnvDefines, "PH_RESTIR_COMBINED_GI", "");
      }

      intDefine(newEnvDefines, "PH_MAX_SAMPLES", properties.getMaxSamples());
   }

   @ModifyArgs(
      method = "lambda$new$8",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"
      ),
      remap = false
   )
   private static void provideSource(Args args) {
      if (isPhotonicsSource) {
         Iterable<StringPair> environmentDefines = (Iterable<StringPair>)args.get(1);
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
