package at.redi2go.photonics.core.rendering;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformHolder;
import at.redi2go.photonics.core.iris.pipeline.uniform.IUniformUpdateFrequency;
import at.redi2go.photonics.core.rendering.lights.HandheldItem;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class HandheldLightComponent implements RenderingComponent {
    private static final Matrix4fc NO_LIGHT = new Matrix4f(
            new Vector4f(0.0f),
            new Vector4f(0.0f),
            new Vector4f(0.0f),
            new Vector4f(0.0f)
    );

    private static final Vector3f ENCHANTMENT_GLINT_COLOR = new Vector3f(0x21 / 255.0f, 0x0D / 255.0f, 0x4F / 255.0f);
    private static final Vector2f ENCHANTMENT_GLINT_ATTENUATION = new Vector2f(0.9f, 0.9f);
    private static final float ENCHANTMENT_GLINT_FALLOFF = 0.75f;

    private final HandheldItemSupplier handheldItemSupplier;

    private final UniformUpdater mainHandUpdater;
    private final HandheldLightCache mainHand;

    private final UniformUpdater offHandUpdater;
    private final HandheldLightCache offHand;

    private final Matrix4fc enchantedItemLight;

    public HandheldLightComponent(
            HandheldItemSupplier handheldItemSupplier,
            PhotonicsProperties properties
    ) {
        this.handheldItemSupplier = handheldItemSupplier;

        this.mainHandUpdater = new UniformUpdater();
        this.mainHand = new HandheldLightCache(handheldItemSupplier::getMainHand, mainHandUpdater);

        this.offHandUpdater = new UniformUpdater();
        this.offHand = new HandheldLightCache(handheldItemSupplier::getOffHand, offHandUpdater);

        var enchantmentGlintStrength = properties.getEnchantmentGlintStrength();
        var enchantmentGlintColor = ENCHANTMENT_GLINT_COLOR.mul(enchantmentGlintStrength, new Vector3f());
        var enchantmentGlintRadius = BlockLightInfo.getBlockRadius(enchantmentGlintColor, ENCHANTMENT_GLINT_ATTENUATION, ENCHANTMENT_GLINT_FALLOFF);

        this.enchantedItemLight = new Matrix4f(
                new Vector4f(0f, 0f, 0f, Float.intBitsToFloat(-1)),
                new Vector4f(enchantmentGlintColor, enchantmentGlintStrength),
                new Vector4f(ENCHANTMENT_GLINT_ATTENUATION, ENCHANTMENT_GLINT_FALLOFF, enchantmentGlintRadius),
                new Vector4f(0f)
        );
    }

    @Override
    public void onFrameBegin() {
        mainHand.update();
        offHand.update();
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        dynamicUniforms.uniform1b(IUniformUpdateFrequency.perFrame(), "left_handed", handheldItemSupplier::isLeftHanded);

        dynamicUniforms.uniform1b(IUniformUpdateFrequency.perFrame(), "main_hand_has_light", () -> !mainHand.isEmpty());
        dynamicUniforms.uniformMatrix("ph_main_hand_light", mainHand::getLightData, mainHandUpdater.newNotifier());

        dynamicUniforms.uniform1b(IUniformUpdateFrequency.perFrame(), "off_hand_has_light", () -> !offHand.isEmpty());
        dynamicUniforms.uniformMatrix("ph_off_hand_light", offHand::getLightData, offHandUpdater.newNotifier());
    }

    private class HandheldLightCache {
        private final Supplier<Optional<HandheldItem>> handheldItemSupplier;
        private final UniformUpdater updater;

        private HandheldItem item;
        private Matrix4fc lightData;

        private HandheldLightCache(
                Supplier<Optional<HandheldItem>> handheldItemSupplier,
                UniformUpdater updater
        ) {
            this.handheldItemSupplier = handheldItemSupplier;
            this.updater = updater;

            this.lightData = NO_LIGHT;
        }

        public boolean isEmpty() {
            return lightData == NO_LIGHT;
        }

        public Matrix4fc getLightData() {
            return lightData;
        }

        private Matrix4fc createEnchantedItemLight(HandheldItem handheldItem) {
            return handheldItem.isEnchanted() ? enchantedItemLight : NO_LIGHT;
        }


        private Matrix4fc createLightFrom(HandheldItem handheldItem) {
            var blockState = handheldItem.getBlockState();
            var light = PhConfig.getLightRegistry().get(blockState);

            return light == null ? createEnchantedItemLight(handheldItem) : light.toMatrix4(
                    new Vector3f(0f),
                    IShaderPack.getCurrentPack()
                        .map(e -> e.getBlockId(blockState))
                        .orElse(-1)
            );
        }

        public void update() {
            var newItem = handheldItemSupplier.get().orElse(null);
            if (Objects.equals(item, newItem)) return;

            this.item = newItem;
            this.lightData = newItem == null ? NO_LIGHT : createLightFrom(newItem);

            updater.updateNow();
        }
    }
}
