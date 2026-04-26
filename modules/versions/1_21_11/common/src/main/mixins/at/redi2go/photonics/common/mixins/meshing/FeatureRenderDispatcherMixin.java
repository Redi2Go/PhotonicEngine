package at.redi2go.photonics.common.mixins.meshing;

import at.redi2go.photonics.common.meshing.FeatureRendererExt;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.feature.ShadowFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.resources.model.AtlasManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureRenderDispatcher.class)
public abstract class FeatureRenderDispatcherMixin implements FeatureRendererExt {
    @Unique
    private boolean renderShadows = true;
    @Unique
    private boolean renderFlames = true;
    @Unique
    private boolean renderNametags = true;
    @Unique
    private boolean renderText = true;
    @Unique
    private boolean renderParticles = true;

    @Override
    public void setRenderShadows(boolean enabled) {
        renderShadows = enabled;
    }

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/ShadowFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            )
    )
    private void renderShadows(
            ShadowFeatureRenderer instance,
            SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            Operation<Void> original
    ) {
        if (!renderShadows) return;

        original.call(instance, submitNodeCollection, bufferSource);
    }

    @Override
    public void setRenderFlames(boolean enabled) {
        renderFlames = enabled;
    }

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/resources/model/AtlasManager;)V"
            )
    )
    private void renderFlames(
            FlameFeatureRenderer instance,
            SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            AtlasManager atlasManager,
            Operation<Void> original
    ) {
        if (!renderFlames) return;

        original.call(
                instance,
                submitNodeCollection,
                bufferSource,
                atlasManager
        );
    }

    @Override
    public void setRenderNametags(boolean enabled) {
        renderNametags = enabled;
    }

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/NameTagFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/gui/Font;)V"
            )
    )
    private void renderNameTags(
            NameTagFeatureRenderer instance,
            SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            Font font,
            Operation<Void> original
    ) {
        if (!renderNametags) return;

        original.call(
                instance,
                submitNodeCollection,
                bufferSource,
                font
        );
    }

    @Override
    public void setRenderText(boolean enabled) {
        renderText = false;
    }

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/TextFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            )
    )
    private void renderText(
            TextFeatureRenderer instance,
            SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            Operation<Void> original
    ) {
        if (!renderText) return;

        original.call(
                instance,
                submitNodeCollection,
                bufferSource
        );
    }

    @Override
    public void setRenderParticles(boolean enabled) {
        renderParticles = enabled;
    }

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/ParticleFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;)V"
            )
    )
    private void renderParticles(
            ParticleFeatureRenderer instance,
            SubmitNodeCollection submitNodeCollection,
            Operation<Void> original
    ) {
        if (!renderParticles) return;

        original.call(instance, submitNodeCollection);
    }
}
