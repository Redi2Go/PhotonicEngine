package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.api.shaders.AlphaMode;
import at.redi2go.photonics.api.shaders.LightingMode;
import at.redi2go.photonics.common.iris.ShaderPropertiesBridge;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static at.redi2go.photonics.api.shaders.PhotonicsProperties.ALPHA_MODE_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.ENABLED_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.ENCHANTMENT_GLINT_STRENGTH_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.IS_BLOCK_LIGHT_ENABLED_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.IS_GI_ENABLED_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.IS_HANDHELD_LIGHT_ENABLED_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.IS_LIGHT_BINNING_ENABLED_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.LIGHTING_MODE_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.MAX_LIGHTS_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.MAX_SAMPLES_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RENDER_SCALE_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_ACCUMULATION_FRAMES_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_COMBINED_GI_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_DENOISER_PASSES_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_INITIAL_SAMPLES_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_SOFT_SHADOWS_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_SPATIAL_REUSE_RADIUS_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.RESTIR_SPATIAL_REUSE_SAMPLES_KEY;
import static at.redi2go.photonics.api.shaders.PhotonicsProperties.SEPARATE_HANDHELD_RAYS_KEY;

@Mixin(ShaderProperties.class)
public abstract class ShaderPropertiesMixin {
    @Inject(
            method = "lambda$new$54",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/properties/ShaderProperties;handleIntDirective(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/function/Consumer;)Z"
            )
    )
    private void initFromProperties(
            CallbackInfo ci,
            @Local(name = "key") String key,
            @Local(name = "value") String value
    ) {
        var phProperties = ShaderPropertiesBridge.consumeProperties();

        handleBooleanDirective(key, value, ENABLED_KEY, e -> phProperties.enabled = e);
        handleFloatDirective(key, value, RENDER_SCALE_KEY, e -> phProperties.renderScale = e);

        handleNonZeroDirective(key, value, MAX_LIGHTS_KEY, e -> phProperties.maxLights = e);
        handleAlphaModeDirective(key, value, ALPHA_MODE_KEY, e -> phProperties.alphaMode = e);
        handleFloatDirective(key, value, ENCHANTMENT_GLINT_STRENGTH_KEY, e -> phProperties.enchantmentGlintStrength = e);
        handleBooleanDirective(key, value, SEPARATE_HANDHELD_RAYS_KEY, e -> phProperties.useSeparateHandheldRays = e);
        handleBooleanDirective(key, value, IS_GI_ENABLED_KEY, e -> phProperties.giEnabled = e);
        handleBooleanDirective(key, value, IS_BLOCK_LIGHT_ENABLED_KEY, e -> phProperties.blockLightEnabled = e);
        handleBooleanDirective(key, value, IS_HANDHELD_LIGHT_ENABLED_KEY, e -> phProperties.handheldLightEnabled = e);
        handleBooleanDirective(key, value, IS_LIGHT_BINNING_ENABLED_KEY, e -> phProperties.lightBinningEnabled = e);

        handleLightingModeDirective(key, value, LIGHTING_MODE_KEY, e -> phProperties.lightingMode = e);

        handleNonZeroDirective(key, value, MAX_SAMPLES_KEY, e -> phProperties.maxSamples = e);

        handleNonZeroDirective(key, value, RESTIR_INITIAL_SAMPLES_KEY, e -> phProperties.restirInitialSamples = e);
        handleNonZeroDirective(key, value, RESTIR_SPATIAL_REUSE_SAMPLES_KEY, e -> phProperties.restirSpatialReuseSamples = e);
        handleFloatDirective(key, value, RESTIR_SPATIAL_REUSE_RADIUS_KEY, e -> phProperties.restirRestirSpatialReuseRadius = e);
        handleNonZeroDirective(key, value, RESTIR_ACCUMULATION_FRAMES_KEY, e -> phProperties.restirAccumulationFrames = e);
        handleUnsignedIntDirective(key, value, RESTIR_DENOISER_PASSES_KEY, e -> phProperties.restirDenoiserPasses = e);
        handleBooleanDirective(key, value, RESTIR_SOFT_SHADOWS_KEY, e -> phProperties.restirSoftShadows = e);
        handleBooleanDirective(key, value, RESTIR_COMBINED_GI_KEY, e -> phProperties.restirCombinedGi = e);
    }

    @Unique
    private static void handleBooleanDirective(String key, String value, String expectedKey, BooleanConsumer handler) {
        if (expectedKey.equals(key)) {
            if (!"true".equals(value) && !"1".equals(value)) {
                if (!"false".equals(value) && !"0".equals(value)) {
                    Iris.logger.warn("Unexpected value for boolean key " + key + " in shaders.properties: got " + value + ", but expected either true or false");
                } else {
                    handler.accept(false);
                }
            } else {
                handler.accept(true);
            }

        }
    }


    @Unique
    private static boolean handleNonZeroDirective(String key, String value, String expectedKey, Consumer<Integer> handler) {
        if (!expectedKey.equals(key)) {
            return false;
        } else {
            try {
                int result = Integer.parseInt(value);
                if (result <= 0)
                    throw new NumberFormatException("Was less than or equal to 0");

                handler.accept(result);
            } catch (NumberFormatException var5) {
                Iris.logger.warn("Unexpected value for unsigned integer key " + key + " in shaders.properties: got " + value + ", but expected an unsigned integer");
            }

            return true;
        }
    }

    @Unique
    private static boolean handleUnsignedIntDirective(String key, String value, String expectedKey, Consumer<Integer> handler) {
        if (!expectedKey.equals(key)) {
            return false;
        } else {
            try {
                int result = Integer.parseInt(value);
                if (result < 0)
                    throw new NumberFormatException("Was negative");

                handler.accept(result);
            } catch (NumberFormatException var5) {
                Iris.logger.warn("Unexpected value for unsigned integer key " + key + " in shaders.properties: got " + value + ", but expected an unsigned integer");
            }

            return true;
        }
    }

    @Unique
    private static boolean handleFloatDirective(String key, String value, String expectedKey, Consumer<Float> handler) {
        if (!expectedKey.equals(key)) {
            return false;
        } else {
            try {
                float result = Float.parseFloat(value);
                handler.accept(result);
            } catch (NumberFormatException var5) {
                Iris.logger.warn("Unexpected value for float key " + key + " in shaders.properties: got " + value + ", but expected a float");
            }

            return true;
        }
    }

    @Unique
    private static boolean handleAlphaModeDirective(String key, String value, String expectedKey, Consumer<AlphaMode> handler) {
        if (!expectedKey.equals(key)) {
            return false;
        } else {
            try {
                AlphaMode result = AlphaMode.valueOf(value.toUpperCase());

                handler.accept(result);
            } catch (IllegalArgumentException var5) {
                Iris.logger.warn("Unexpected value for alpha mode key " + key + " in shaders.properties: got " + value + ", but expected alpha mode");
            }

            return true;
        }
    }

    @Unique
    private static boolean handleLightingModeDirective(String key, String value, String expectedKey, Consumer<LightingMode> handler) {
        if (!expectedKey.equals(key)) {
            return false;
        } else {
            try {
                LightingMode result = LightingMode.valueOf(value.toUpperCase());

                handler.accept(result);
            } catch (IllegalArgumentException var5) {
                Iris.logger.warn("Unexpected value for lighting mode key " + key + " in shaders.properties: got " + value + ", but expected lighting mode");
            }

            return true;
        }
    }
}
