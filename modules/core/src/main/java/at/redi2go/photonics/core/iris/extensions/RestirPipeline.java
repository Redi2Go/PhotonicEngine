package at.redi2go.photonics.core.iris.extensions;

import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.core.iris.AbstractPhotonicsExtension;
import at.redi2go.photonics.core.iris.Pipelines;
import at.redi2go.photonics.core.iris.pipeline.rendering.IrisFactory;
import at.redi2go.photonics.core.iris.pipeline.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class RestirPipeline extends AbstractPhotonicsExtension {
    private final int denoiserPasses;

    private int atrousIteration = 0;
    private final UniformUpdater atrousUpdater = new UniformUpdater();

    public RestirPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisFactory irisFactory
    ) {
        super(properties, atlasDownloader);

        // The hand always needs at least 7 denoiser passes.
        int requestedDenoiserPasses = properties.getRestirDenoiserPasses();

        // Subtract one as denoising is done in reverse order (largest radius -> smallest radius)
        this.denoiserPasses = (requestedDenoiserPasses != 0 ? Math.max(requestedDenoiserPasses, 7) : 0) - 1;

        Pipelines.fragData(this, properties, irisFactory);
        //Pipelines.handheldLighting(this, handheldItemSupplier, properties, irisFactory);

//        neighborSelectionPipeline(irisFactory);
        restirDiPipeline(irisFactory);
//        restirGiPipeline(irisFactory);
//        svgfPipeline(irisFactory);

        Pipelines.exposureHistory(this, irisFactory);
    }

    private void neighborSelectionPipeline(IrisFactory irisFactory) {
        if (!isAnySpatialReuseEnabled()) return;

        var framebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("neighbor_reservoir", ITextureFormat.rgba32f(), CREATE_SAMPLER)
                .build(this::registerComponent);

        irisFactory.newPipeline()
                .debugGroup("neighbor selection")
                .withFragmentPrefix("/photonics/rendering/restir/neighbor/passes/")
                .withFramebuffer(framebuffer)
                .deferredPass("neighbor selection", "ns0_neighbor_selection.fsh", null)
                .build(this::registerRenderer);
    }

    private void restirDiPipeline(IrisFactory irisFactory) {
        if (!isBlockLightEnabled()) return;

        var framebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("di_reservoirs0", ITextureFormat.rgb32f(), CREATE_SAMPLER | FLIP)
                .addAttachment("di_output", ITextureFormat.rgb16f(), CREATE_SAMPLER)
                .build(this::registerComponent);

        irisFactory.newPipeline()
                .debugGroup("restir di")
                .withFragmentPrefix("/photonics/rendering/restir/direct/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("initial direct", "di0_initial_direct.fsh", null)
                .deferredPass("temporal reuse", "di1_temporal_reuse.fsh", null)

//                .when(this::isDiSpatialReuseEnabled, b0 -> {
//                    b0.thenFlip(framebuffer);
//                    b0.deferredPass("spatial reuse", "di2_temporal_reuse.fsh", null);
//                    b0.thenFlip(framebuffer);
//                })
                .deferredPass("validate visibility", "di3_validate_visibility.fsh", null)
                .build(this::registerRenderer);
    }

    private void restirGiPipeline(IrisFactory irisFactory) {
        if (!isRestirGiEnabled()) return;

        var framebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("gi_reservoirs0", ITextureFormat.rgba32f(), CREATE_SAMPLER | FLIP)
                .addAttachment("gi_reservoirs1", ITextureFormat.rgb32ui(), CREATE_SAMPLER | FLIP)
                .addAttachment("gi_output", ITextureFormat.rgb16f(), CREATE_SAMPLER)
                .build(this::registerComponent);

        irisFactory.newPipeline()
                .debugGroup("restir gi")
                .withFragmentPrefix("/photonics/rendering/restir/indirect/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("initial indirect", "gi0_initial_direct.fsh", null)
                .deferredPass("temporal reuse", "gi1_temporal_reuse.fsh", null)
                .when(this::isGiSpatialReuseEnabled, b0 -> {
                    b0.thenFlip(framebuffer);
                    b0.deferredPass("spatial reuse", "gi2_temporal_reuse.fsh", null);
                    b0.thenFlip(framebuffer);
                })
                .deferredPass("validate visibility", "gi3_validate_visibility.fsh", null)
                .build(this::registerRenderer);
    }

    private void svgfPipeline(IrisFactory irisFactory) {
        if (!isRestirEnabled()) return;

        var framebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("diffuse_history", ITextureFormat.rgba32ui(), CREATE_SAMPLER | FLIP)
                .addAttachment("denoise_result", ITextureFormat.rgba32ui(), CREATE_SAMPLER | FLIP, this::isDenoisingEnabled)
                .build(this::registerComponent);

        irisFactory.newPipeline()
                .debugGroup("svgf")
                .withFragmentPrefix("/photonics/rendering/restir/svgf/passes/")
                .withFramebuffer(framebuffer)
                .deferredPass("accumulation", "sv0_accumulation.fsh", null)
                .deferredPass("variance prefilter", "sv1_variance_prefilter.fsh", null, this::isDenoisingEnabled)
                .repeat(denoiserPasses, (i, b0) -> {
                    int index = denoiserPasses - i;

                    b0.thenRun(() -> atrousIteration = index);
                    b0.thenRun(atrousUpdater::updateNow);
                    b0.thenFlip(framebuffer);
                    b0.deferredPass("atrous iteration #" + index, "sv2_atrous.fsh", null, this::isDenoisingEnabled);
                })
                .deferredPass("undo exposure", "sv3_undo_exposure.fsh", null, this::isDenoisingEnabled)
                .build(this::registerRenderer);
    }

    @Override
    public void registerDynamicUniforms(IDynamicUniformHolder dynamicUniforms) {
        super.registerDynamicUniforms(dynamicUniforms);

        dynamicUniforms.uniform1i(
                "atrous_iteration",
                () -> atrousIteration,
                atrousUpdater.newNotifier()
        );
    }

    public boolean isBlockLightEnabled() {
        return properties.isBlockLightEnabled();
    }

    public boolean isHandheldLightingEnabled() {
        return properties.isHandheldLightEnabled();
    }

    public boolean isRestirGiEnabled() {
        return properties.isGiEnabled() && properties.useRestirCombinedGi();
    }

    public boolean isRestirEnabled() {
        return isBlockLightEnabled() || isRestirGiEnabled();
    }

    public boolean isDiSpatialReuseEnabled() {
        return isBlockLightEnabled() && properties.getRestirSpatialReuseSamples() > 0;
    }

    public boolean isGiSpatialReuseEnabled() {
        return isRestirGiEnabled() && properties.getRestirSpatialReuseSamples() > 0;
    }

    public boolean isAnySpatialReuseEnabled() {
        return isDiSpatialReuseEnabled() || isGiSpatialReuseEnabled();
    }

    public boolean isDenoisingEnabled() {
        return isRestirEnabled() && denoiserPasses >= 0;
    }
}
