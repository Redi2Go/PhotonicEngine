package at.redi2go.photonics.common.mixins.iris.pipeline;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.common.AtlasDownloaderImpl;
import at.redi2go.photonics.common.iris.pipeline.IrisPipelineFactoryImpl;
import at.redi2go.photonics.common.iris.pipeline.IrisRenderingPipelineExt;
import at.redi2go.photonics.common.iris.pipeline.PipelineManagerExt;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.PhotonicsRenderer;
import at.redi2go.photonics.common.meshing.MinecraftBlockMesher;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(PipelineManager.class)
public abstract class PipelineManagerMixin implements PipelineManagerExt {
    @Shadow
    private WorldRenderingPipeline pipeline;
    @Unique
    private PhotonicsExtension photonics;

    @Unique
    private List<IrisRendererImpl> renderers = new ArrayList<>();

    @Inject(method = "preparePipeline", at = @At("HEAD"))
    private void preparePipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (photonics != null) return;

        //TODO Add to more sensible spot
        BlockMesher.REGISTRY.addDefault(new MinecraftBlockMesher());

        var shaderPack = (IShaderPack) Iris.getCurrentPack().orElse(null);
        if (shaderPack == null) return;

        var properties = shaderPack.properties();
        photonics = PhotonicsExtension.create(
                properties,
                AtlasDownloaderImpl::new,
                new IrisPipelineFactoryImpl(renderers)
        );
    }

    @Inject(method = "preparePipeline", at = @At("TAIL"))
    private void selectPipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (pipeline instanceof IrisRenderingPipeline ext)
            ((IrisRenderingPipelineExt) ext).onSelect();
        else
            clearRenderers();
    }

    @Override
    public Optional<PhotonicsExtension> photonics() {
        return Optional.ofNullable(photonics);
    }

    @Override
    public List<IrisRendererImpl> getRenderers() {
        return renderers;
    }

    @Unique
    private void clearRenderers() {
        for (var renderer : renderers)
            renderer.setActive(null);
    }

    @Override
    public void setRenderers(@Nullable List<PhotonicsRenderer> activeRenderers) {
        if (activeRenderers == null) {
            clearRenderers();
            return;
        }

        if (activeRenderers.size() != renderers.size())
            throw new IllegalArgumentException("unexpected active renderers size");

        for (int i = 0; i < activeRenderers.size(); i++)
            renderers.get(i).setActive(activeRenderers.get(i));
    }

    @Inject(method = "destroyPipeline", at = @At("HEAD"))
    private void destroyEverything(CallbackInfo ci) {
        if (photonics != null) {
            photonics.close();
            photonics = null;

            clearRenderers();
            renderers.clear();
        }
    }
}
