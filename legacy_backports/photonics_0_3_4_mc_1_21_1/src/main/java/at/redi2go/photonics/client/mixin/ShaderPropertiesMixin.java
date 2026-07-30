package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.api.AlphaMode;
import at.redi2go.photonics.client.api.LightingMode;
import at.redi2go.photonics.client.api.PhotonicsProperties;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Consumer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderProperties.class)
public abstract class ShaderPropertiesMixin implements PhotonicsProperties {
   @Unique
   private ShaderPackOptions photonics$shaderPackOptions;
   @Unique
   private OptionalBoolean isPhotonicsEnabled;
   @Unique
   private float renderScale;
   @Unique
   private int maxLights;
   @Unique
   private AlphaMode alphaMode;
   @Unique
   private float enchantmentGlintStrength;
   @Unique
   private OptionalBoolean useSeparateHandheldRays;
   @Unique
   private OptionalBoolean isGiEnabled;
   @Unique
   private OptionalBoolean isBlockLightEnabled;
   @Unique
   private OptionalBoolean isHandheldLightEnabled;
   @Unique
   private OptionalBoolean voxelizeLava;
   @Unique
   private OptionalBoolean enableLightBinning;
   @Unique
   private LightingMode lightingMode;
   @Unique
   private int restirInitialSamples;
   @Unique
   private int restirSpatialReuseSamples;
   @Unique
   private float restirSpatialReuseRadius;
   @Unique
   private int restirAccumulationFrames;
   @Unique
   private OptionalBoolean restirSoftShadows;
   @Unique
   private OptionalBoolean restirCombinedGi;
   @Unique
   private int denoiserPasses;
   @Unique
   private int maxSamples;

   @Inject(method = "<init>()V", at = @At("TAIL"))
   private void defaultInit(CallbackInfo ci) {
      this.isPhotonicsEnabled = OptionalBoolean.DEFAULT;
      this.renderScale = 1.0F;
      this.maxLights = 1000;
      this.alphaMode = PhotonicsProperties.DEFAULT_ALPHA_MODE;
      this.enchantmentGlintStrength = 0.2F;
      this.useSeparateHandheldRays = OptionalBoolean.DEFAULT;
      this.isGiEnabled = OptionalBoolean.DEFAULT;
      this.isBlockLightEnabled = OptionalBoolean.DEFAULT;
      this.isHandheldLightEnabled = OptionalBoolean.DEFAULT;
      this.enableLightBinning = OptionalBoolean.DEFAULT;
      this.voxelizeLava = OptionalBoolean.DEFAULT;
      this.lightingMode = PhotonicsProperties.DEFAULT_LIGHTING_MODE;
      this.restirInitialSamples = 32;
      this.restirSpatialReuseSamples = 5;
      this.restirSpatialReuseRadius = 10.0F;
      this.restirAccumulationFrames = 32;
      this.restirSoftShadows = OptionalBoolean.DEFAULT;
      this.restirCombinedGi = OptionalBoolean.DEFAULT;
      this.denoiserPasses = 5;
      this.maxSamples = 20;
   }

   @Inject(method = "<init>(Ljava/lang/String;Lnet/irisshaders/iris/shaderpack/option/ShaderPackOptions;Ljava/lang/Iterable;)V", at = @At("CTOR_HEAD"))
   private void defaultInit2(String contents, ShaderPackOptions shaderPackOptions, Iterable environmentDefines, CallbackInfo ci) {
      this.photonics$shaderPackOptions = shaderPackOptions;
      this.isPhotonicsEnabled = OptionalBoolean.DEFAULT;
      this.renderScale = 1.0F;
      this.maxLights = 1000;
      this.alphaMode = PhotonicsProperties.DEFAULT_ALPHA_MODE;
      this.enchantmentGlintStrength = 0.2F;
      this.useSeparateHandheldRays = OptionalBoolean.DEFAULT;
      this.isGiEnabled = OptionalBoolean.DEFAULT;
      this.isBlockLightEnabled = OptionalBoolean.DEFAULT;
      this.isHandheldLightEnabled = OptionalBoolean.DEFAULT;
      this.enableLightBinning = OptionalBoolean.DEFAULT;
      this.voxelizeLava = OptionalBoolean.DEFAULT;
      this.lightingMode = PhotonicsProperties.DEFAULT_LIGHTING_MODE;
      this.restirInitialSamples = 32;
      this.restirSpatialReuseSamples = 5;
      this.restirSpatialReuseRadius = 10.0F;
      this.restirAccumulationFrames = 32;
      this.restirSoftShadows = OptionalBoolean.DEFAULT;
      this.restirCombinedGi = OptionalBoolean.DEFAULT;
      this.denoiserPasses = 5;
      this.maxSamples = 20;
   }

   @Inject(
      method = "lambda$new$58(Ljava/lang/Object;Ljava/lang/Object;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/shaderpack/properties/ShaderProperties;handleIntDirective(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/function/Consumer;)Z"
      )
   )
   private void initFromProperties(CallbackInfo ci, @Local(name = "key") String key, @Local(name = "value") String value) {
      value = resolveOptionValue(this.photonics$shaderPackOptions, value);
      handleBooleanDirective(key, value, "photonics.enabled", e -> this.isPhotonicsEnabled = e);
      handleFloatDirective(key, value, "photonics.renderScale", e -> this.renderScale = e);
      handleNonZeroDirective(key, value, "photonics.maxLights", e -> this.maxLights = e);
      handleAlphaModeDirective(key, value, "photonics.alphaMode", e -> this.alphaMode = e);
      handleFloatDirective(key, value, "photonics.enchantmentGlintStrength", e -> this.enchantmentGlintStrength = e);
      handleBooleanDirective(key, value, "photonics.useSeparateHandheldRays", e -> this.useSeparateHandheldRays = e);
      handleBooleanDirective(key, value, "photonics.enableGi", e -> this.isGiEnabled = e);
      handleBooleanDirective(key, value, "photonics.enableBlockLight", e -> this.isBlockLightEnabled = e);
      handleBooleanDirective(key, value, "photonics.enableHandheldLight", e -> this.isHandheldLightEnabled = e);
      handleBooleanDirective(key, value, "photonics.enableLightBinning", e -> this.enableLightBinning = e);
      handleBooleanDirective(key, value, "photonics.voxelizeLava", e -> this.voxelizeLava = e);
      handleLightingModeDirective(key, value, "photonics.lightingMode", e -> this.lightingMode = e);
      handleNonZeroDirective(key, value, "photonics.restirInitialSamples", e -> this.restirInitialSamples = e);
      handleNonZeroDirective(key, value, "photonics.restirSpatialReuseSamples", e -> this.restirSpatialReuseSamples = e);
      handleFloatDirective(key, value, "photonics.restirSpatialReuseRadius", e -> this.restirSpatialReuseRadius = e);
      handleNonZeroDirective(key, value, "photonics.restirAccumulationFrames", e -> this.restirAccumulationFrames = e);
      handleUnsignedIntDirective(key, value, "photonics.restirDenoiserPasses", e -> this.denoiserPasses = e);
      handleBooleanDirective(key, value, "photonics.restirSoftShadows", e -> this.restirSoftShadows = e);
      handleBooleanDirective(key, value, "photonics.restirCombinedGi", e -> this.restirCombinedGi = e);
      handleNonZeroDirective(key, value, "photonics.maxSamples", e -> this.maxSamples = e);
   }

   @Unique
   private static String resolveOptionValue(ShaderPackOptions shaderPackOptions, String value) {
      if (shaderPackOptions == null) {
         return value;
      }

      OptionValues optionValues = shaderPackOptions.getOptionValues();
      if (optionValues.getOptionSet().getStringOptions().containsKey(value)) {
         return optionValues.getStringValueOrDefault(value);
      }
      if (optionValues.getOptionSet().isBooleanOption(value)) {
         return Boolean.toString(optionValues.getBooleanValueOrDefault(value));
      }
      return value;
   }

   @Override
   public OptionalBoolean isPhotonicsEnabled() {
      return this.isPhotonicsEnabled;
   }

   @Override
   public float getRenderScale() {
      return this.renderScale;
   }

   @Override
   public int getMaxLights() {
      return this.maxLights;
   }

   @Override
   public AlphaMode getAlphaMode() {
      return this.alphaMode;
   }

   @Override
   public float getEnchantmentGlintStrength() {
      return this.enchantmentGlintStrength;
   }

   @Override
   public OptionalBoolean useSeparateHandheldRays() {
      return this.useSeparateHandheldRays;
   }

   @Override
   public OptionalBoolean isGiEnabled() {
      return this.isGiEnabled;
   }

   @Override
   public OptionalBoolean isBlockLightEnabled() {
      return this.isBlockLightEnabled;
   }

   @Override
   public OptionalBoolean isHandheldLightEnabled() {
      return this.isHandheldLightEnabled;
   }

   @Override
   public OptionalBoolean isLightBinningEnabled() {
      return this.enableLightBinning;
   }

   @Override
   public OptionalBoolean voxelizeLava() {
      return this.voxelizeLava;
   }

   @Override
   public LightingMode getLightingMode() {
      return this.lightingMode;
   }

   @Override
   public int getRestirInitialSamples() {
      return this.restirInitialSamples;
   }

   @Override
   public int getRestirSpatialReuseSamples() {
      return this.restirSpatialReuseSamples;
   }

   @Override
   public float getRestirSpatialReuseRadius() {
      return this.restirSpatialReuseRadius;
   }

   @Override
   public int getRestirAccumulationFrames() {
      return this.restirAccumulationFrames;
   }

   @Override
   public OptionalBoolean useRestirSoftShadows() {
      return this.restirSoftShadows;
   }

   @Override
   public OptionalBoolean useRestirCombinedGi() {
      return this.restirCombinedGi;
   }

   @Override
   public int getRestirDenoiserPasses() {
      return this.denoiserPasses;
   }

   @Override
   public int getMaxSamples() {
      return this.maxSamples;
   }

   @Shadow
   private static void handleBooleanDirective(String key, String value, String expectedKey, Consumer<OptionalBoolean> handler) {
      throw new AssertionError();
   }

   @Unique
   private static boolean handleNonZeroDirective(String key, String value, String expectedKey, Consumer<Integer> handler) {
      if (!expectedKey.equals(key)) {
         return false;
      }

      try {
         int result = Integer.parseInt(value);
         if (result <= 0) {
            throw new NumberFormatException("Was less than or equal to 0");
         }

         handler.accept(result);
      } catch (NumberFormatException var5) {
         Iris.logger.warn("Unexpected value for unsigned integer key " + key + " in shaders.properties: got " + value + ", but expected an unsigned integer");
      }

      return true;
   }

   @Unique
   private static boolean handleUnsignedIntDirective(String key, String value, String expectedKey, Consumer<Integer> handler) {
      if (!expectedKey.equals(key)) {
         return false;
      }

      try {
         int result = Integer.parseInt(value);
         if (result < 0) {
            throw new NumberFormatException("Was negative");
         }

         handler.accept(result);
      } catch (NumberFormatException var5) {
         Iris.logger.warn("Unexpected value for unsigned integer key " + key + " in shaders.properties: got " + value + ", but expected an unsigned integer");
      }

      return true;
   }

   @Unique
   private static boolean handleFloatDirective(String key, String value, String expectedKey, Consumer<Float> handler) {
      if (!expectedKey.equals(key)) {
         return false;
      }

      try {
         float result = Float.parseFloat(value);
         handler.accept(result);
      } catch (NumberFormatException var5) {
         Iris.logger.warn("Unexpected value for float key " + key + " in shaders.properties: got " + value + ", but expected a float");
      }

      return true;
   }

   @Unique
   private static boolean handleAlphaModeDirective(String key, String value, String expectedKey, Consumer<AlphaMode> handler) {
      if (!expectedKey.equals(key)) {
         return false;
      }

      try {
         AlphaMode result = AlphaMode.valueOf(value.toUpperCase());
         handler.accept(result);
      } catch (IllegalArgumentException var5) {
         Iris.logger.warn("Unexpected value for alpha mode key " + key + " in shaders.properties: got " + value + ", but expected alpha mode");
      }

      return true;
   }

   @Unique
   private static boolean handleLightingModeDirective(String key, String value, String expectedKey, Consumer<LightingMode> handler) {
      if (!expectedKey.equals(key)) {
         return false;
      }

      try {
         LightingMode result = LightingMode.valueOf(value.toUpperCase());
         handler.accept(result);
      } catch (IllegalArgumentException var5) {
         Iris.logger.warn("Unexpected value for lighting mode key " + key + " in shaders.properties: got " + value + ", but expected lighting mode");
      }

      return true;
   }
}
