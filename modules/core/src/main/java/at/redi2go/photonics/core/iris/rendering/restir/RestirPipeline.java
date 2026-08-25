package at.redi2go.photonics.core.iris.rendering.restir;

import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.core.iris.pipeline.IrisPipeline;
import at.redi2go.photonics.core.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.core.iris.rendering.PhotonicsPipeline;
import at.redi2go.photonics.core.iris.rendering.Pipelines;
import at.redi2go.photonics.core.rendering.UniformUpdater;
import at.redi2go.photonics.core.rendering.lights.HandheldItemSupplier;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class RestirPipeline extends PhotonicsPipeline {
    private final int denoiserPasses;

    private final RestirProperties restirProperties;

    public RestirPipeline(
            PhotonicsProperties phProperties,
            RestirProperties restirProperties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisPipeline irisPipeline
    ) {
        super(phProperties, atlasDownloader, irisPipeline);
        this.restirProperties = restirProperties;

        // The hand always needs at least 7 denoiser passes.
        int requestedDenoiserPasses = restirProperties.getDenoiserPasses();

        // Subtract one as denoising is done in reverse order (largest radius -> smallest radius)
        this.denoiserPasses = (requestedDenoiserPasses != 0 ? Math.max(requestedDenoiserPasses, 7) : 0) - 1;

        Pipelines.fragData(this, phProperties, irisPipeline);
        Pipelines.handheldLighting(this, handheldItemSupplier, phProperties, irisPipeline);

        neighborSelectionPipeline(irisPipeline);
        restirDiPipeline(irisPipeline);
        restirGiPipeline(irisPipeline);
        svgfPipeline(irisPipeline);

        Pipelines.exposureHistory(this, irisPipeline);
    }

    private void neighborSelectionPipeline(IrisPipeline irisPipeline) {
        if (!isAnySpatialReuseEnabled()) return;

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("neighbor_reservoir", TextureFormat.RGBA32F, CREATE_SAMPLER)
                .build(this::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("neighbor selection")
                .withFragmentPrefix("/photonics/rendering/restir/neighbor/passes/")
                .withFramebuffer(framebuffer)
                .deferredPass("neighbor selection", "ns0_neighbor_selection.fsh", null)
                .build(this::registerRenderer);
    }

    private void restirDiPipeline(IrisPipeline irisPipeline) {
        if (!isBlockLightEnabled()) return;

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("di_reservoirs0", TextureFormat.RGBA32F, CREATE_SAMPLER | FLIP)
                .addAttachment("di_output", TextureFormat.RGBA16F, CREATE_SAMPLER)
                .build(this::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("restir di")
                .withFragmentPrefix("/photonics/rendering/restir/direct/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("initial direct", "di0_initial_direct.fsh", null)
                .deferredPass("temporal reuse", "di1_temporal_reuse.fsh", null)
                .when(this::isDiSpatialReuseEnabled, b0 -> {
                    b0.thenFlip(framebuffer);
                    b0.deferredPass("spatial reuse", "di2_spatial_reuse.fsh", null);
                    b0.thenFlip(framebuffer);
                })
                .deferredPass("validate visibility", "di3_validate_visibility.fsh", null)
                .build(this::registerRenderer);
    }

    private void restirGiPipeline(IrisPipeline irisPipeline) {
        if (!isRestirGiEnabled()) return;

        var framebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("gi_reservoirs0", TextureFormat.RGBA32F, CREATE_SAMPLER | FLIP)
                .addAttachment("gi_reservoirs1", TextureFormat.RGBA32UI, CREATE_SAMPLER | FLIP)
                .addAttachment("gi_output", TextureFormat.RGBA16F, CREATE_SAMPLER)
                .build(this::registerComponent);

        irisPipeline.newRenderer()
                .debugGroup("restir gi")
                .withFragmentPrefix("/photonics/rendering/restir/indirect/passes/")
                .withFramebuffer(framebuffer)
                .thenFlip(framebuffer)
                .deferredPass("initial indirect", "gi0_initial_indirect.fsh", null)
                .deferredPass("temporal reuse", "gi1_temporal_reuse.fsh", null)
                .when(this::isGiSpatialReuseEnabled, b0 -> {
                    b0.thenFlip(framebuffer);
                    b0.deferredPass("spatial reuse", "gi2_spatial_reuse.fsh", null);
                    b0.thenFlip(framebuffer);
                })
                .deferredPass("validate visibility", "gi3_validate_visibility.fsh", null)
                .build(this::registerRenderer);
    }

    private void svgfPipeline(IrisPipeline irisPipeline) {
        if (!isRestirEnabled()) return;

        var temporalFramebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("diffuse_history", TextureFormat.RGBA32UI, CREATE_SAMPLER | FLIP)
                .addAttachment("fast_diffuse_history", isDenoisingEnabled() ? TextureFormat.RGBA16F : TextureFormat.RGBA32F, CREATE_SAMPLER | FLIP)
                .addAttachment("visibility_history", TextureFormat.R16F, CREATE_SAMPLER | FLIP)
                .build(this::registerComponent);

        var denoiseFramebuffer = irisPipeline.newFramebuffer(properties.getRenderScale())
                .addAttachment("denoise_result", TextureFormat.RGBA32UI, CREATE_SAMPLER | FLIP, this::isDenoisingEnabled)
                .build(this::registerComponent);

        int[] atrousIteration = new int[] {0};
        final UniformUpdater atrousUpdater = new UniformUpdater();

        irisPipeline.newRenderer()
                .debugGroup("svgf")
                .dynamicUniform1i("atrous_iteration", () -> atrousIteration[0], atrousUpdater.newNotifier())
                .withFragmentPrefix("/photonics/rendering/restir/svgf/passes/")
                .withFramebuffer(temporalFramebuffer)
                .thenFlip(temporalFramebuffer)
                .deferredPass("accumulation", "sv0_accumulation.fsh", null)
                .withFramebuffer(denoiseFramebuffer)
                .deferredPass("variance prefilter", "sv1_variance_prefilter.fsh", null, this::isDenoisingEnabled)
                .repeat(denoiserPasses, (i, b0) -> {
                    int index = denoiserPasses - i;

                    b0.thenRun(() -> atrousIteration[0] = index);
                    b0.thenRun(atrousUpdater::updateNow);
                    b0.thenFlip(denoiseFramebuffer);
                    b0.deferredPass("atrous iteration #" + index, "sv2_atrous.fsh", null, this::isDenoisingEnabled);
                })
                .deferredPass("undo exposure", "sv3_undo_exposure.fsh", null, this::isDenoisingEnabled)
                .build(this::registerRenderer);
    }

    public boolean isBlockLightEnabled() {
        return properties.getBlockLightProperties().isEnabled();
    }

    public boolean isHandheldLightingEnabled() {
        return properties.getHandheldProperties().isEnabled();
    }

    public boolean isRestirGiEnabled() {
        return properties.getGiProperties().isEnabled() &&
                restirProperties.getGiProperties().isEnabled();
    }

    public boolean isRestirEnabled() {
        return isBlockLightEnabled() || isRestirGiEnabled();
    }

    public boolean isDiSpatialReuseEnabled() {
        return isBlockLightEnabled() && restirProperties.getSpatialReuseSamples() > 0;
    }

    public boolean isGiSpatialReuseEnabled() {
        return isRestirGiEnabled() && restirProperties.getSpatialReuseSamples() > 0;
    }

    public boolean isAnySpatialReuseEnabled() {
        return isDiSpatialReuseEnabled() || isGiSpatialReuseEnabled();
    }

    public boolean isDenoisingEnabled() {
        return isRestirEnabled() && denoiserPasses >= 0;
    }
}
