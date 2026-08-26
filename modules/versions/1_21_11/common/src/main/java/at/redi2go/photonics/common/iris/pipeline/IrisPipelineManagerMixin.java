package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.IrisAtlasDownloader;
import at.redi2go.photonics.common.iris.pipeline.renderer.DeferredIrisPassAction;
import at.redi2go.photonics.common.iris.pipeline.renderer.composite.PhCompositeRenderer;
import at.redi2go.photonics.engine.Photonics;
import at.redi2go.photonics.engine.config.PhConfig;
import at.redi2go.photonics.engine.config.lights.LightsProvider;
import at.redi2go.photonics.engine.iris.AbstractIrisPackLights;
import at.redi2go.photonics.engine.iris.IrisManager;
import at.redi2go.photonics.engine.iris.IrisPack;
import at.redi2go.photonics.engine.iris.IrisPackPath;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.ShaderPack;
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

@Mixin(PipelineManager.class)
public abstract class IrisPipelineManagerMixin implements IrisPipelineManagerExt {
    @Shadow private WorldRenderingPipeline pipeline;

    @Unique private @Nullable LightsProvider lightsProvider;

    @Unique private final List<DeferredIrisPassAction> renderers = new ArrayList<>();

    @Inject(method = "preparePipeline", at = @At("HEAD"))
    private void preparePipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (IrisManager.hasPipeline()) return;

        //TODO Add to more sensible spot
//        BlockMesher.REGISTRY.addDefault(new MinecraftBlockMesher());

        var shaderPack = (IrisPack) Iris.getCurrentPack().orElse(null);
        if (shaderPack == null) return;

        lightsProvider = readLightsProvider(shaderPack);
        if (lightsProvider != null)
            PhConfig.registerLightProvider(lightsProvider);

        IrisManager.preparePipeline(IrisAtlasDownloader::new, new IrisPipelineImpl(renderers));
    }

    @Unique
    private @Nullable LightsProvider readLightsProvider(IrisPack pack) {
        var contents = pack.readFile(IrisPackPath.fromAbsolutePath("/ph_lights.json"));
        if (contents == null) return null;

        try {
            var lights = AbstractIrisPackLights.parse(contents, IrisPackLightsImpl.class);
            lights.setShaderPack((ShaderPack) pack);

            return lights;
        } catch (Exception e) {
            Photonics.LOGGER.error("Error while parsing ph_lights.json for {}", pack.ph$name(), e);
            return null;
        }
    }

    @Inject(method = "preparePipeline", at = @At("TAIL"))
    private void selectPipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (!(pipeline instanceof IrisRenderingPipeline)) clearRenderers();

        setRenderers(((IrisRenderingPipelineExt) pipeline).getRenderers());
    }

    @Override
    public List<DeferredIrisPassAction> getRenderers() {
        return renderers;
    }

    @Unique
    private void clearRenderers() {
        for (var renderer : renderers)
            renderer.setActive(null);
    }

    @Unique
    private void setRenderers(@Nullable List<PhCompositeRenderer> activeRenderers) {
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
        if (IrisManager.hasPipeline()) {
            IrisManager.destroyEverything();
            renderers.clear();
        }

        if (lightsProvider != null) {
            PhConfig.removeLightProvider(lightsProvider);
            lightsProvider = null;
        }
    }
}
