package at.redi2go.photonics.common.mixins.iris.pipeline;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.common.AtlasDownloaderImpl;
import at.redi2go.photonics.common.HandheldLightSupplierImpl;
import at.redi2go.photonics.common.iris.IrisPackLightsImpl;
import at.redi2go.photonics.common.iris.pipeline.IrisPipelineFactoryImpl;
import at.redi2go.photonics.common.iris.pipeline.IrisRenderingPipelineExt;
import at.redi2go.photonics.common.iris.pipeline.PipelineManagerExt;
import at.redi2go.photonics.common.iris.pipeline.renderer.IrisRendererImpl;
import at.redi2go.photonics.common.iris.pipeline.renderer.PhotonicsRenderer;
import at.redi2go.photonics.common.meshing.MinecraftBlockMesher;
import at.redi2go.photonics.common.mixins.iris.ShaderPackAccessor;
import at.redi2go.photonics.core.Photonics;
import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.lights.LightsProvider;
import at.redi2go.photonics.core.iris.AbstractIrisPackLights;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
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
    private @Nullable LightsProvider lightsProvider;

    @Unique
    private List<IrisRendererImpl> renderers = new ArrayList<>();

    @Inject(method = "preparePipeline", at = @At("HEAD"))
    private void preparePipeline(NamespacedId currentDimension, CallbackInfoReturnable<WorldRenderingPipeline> cir) {
        if (photonics != null) return;

        //TODO Add to more sensible spot
        BlockMesher.REGISTRY.addDefault(new MinecraftBlockMesher());

        var shaderPack = (IShaderPack) Iris.getCurrentPack().orElse(null);
        if (shaderPack == null) return;

        lightsProvider = readLightsProvider(shaderPack);
        if (lightsProvider != null)
            PhConfig.registerLightProvider(lightsProvider);

        var properties = shaderPack.properties();
        photonics = PhotonicsExtension.create(
                properties,
                AtlasDownloaderImpl::new,
                HandheldLightSupplierImpl::new,
                new IrisPipelineFactoryImpl(renderers)
        );
    }

    @Unique
    private @Nullable LightsProvider readLightsProvider(IShaderPack pack) {
        var contents = ((ShaderPackAccessor) pack).getSourceProvider()
                .apply(AbsolutePackPath.fromAbsolutePath("/ph_lights.json"));

        if (contents == null)
            return null;

        try {
            var lights = AbstractIrisPackLights.parse(contents, IrisPackLightsImpl.class);
            lights.setShaderPack((ShaderPack) pack);

            return lights;
        } catch (Exception e) {
            Photonics.LOGGER.error("Error while parsing ph_lights.json for {}", pack.name(), e);
            return null;
        }
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

        if (lightsProvider != null) {
            PhConfig.removeLightProvider(lightsProvider);
            lightsProvider = null;
        }
    }
}
