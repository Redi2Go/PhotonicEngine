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

import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_PREV_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.CREATE_SAMPLER;
import static at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage.FLIP;

public class RestirPipeline extends AbstractPhotonicsExtension {
//    private final IrisRenderer fragRenderer;
//
//    private final IrisFramebuffer restirFramebuffer;
//    private final IrisRenderer restirRenderer;
//

//
//    private final IrisFramebuffer denoiseFramebuffer;
//
//    private final IrisRenderer denoisePrepassRenderer;
//    private final IrisRenderer denoiseRenderer;
//
//    private final IrisFramebuffer otherFramebuffer;
//    private final IrisRenderer otherRenderer;

    private final int denoiserPasses;

    private int atrousIteration = 0;
    private final UniformUpdater atrousUpdater = new UniformUpdater();

    public RestirPipeline(
            PhotonicsProperties properties,
            AtlasDownloader atlasDownloader,
            HandheldItemSupplier handheldItemSupplier,
            IrisFactory irisFactory
    ) {
        super(properties, atlasDownloader, handheldItemSupplier);

        // The hand always needs at least 7 denoiser passes.
        int requestedDenoiserPasses = properties.getRestirDenoiserPasses();
        this.denoiserPasses = requestedDenoiserPasses != 0 ? Math.max(requestedDenoiserPasses, 7) : 0;

        var restirFramebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("restir_lighting", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirEnabled)
                .addAttachment("restir_lighting_variance", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirEnabled)
                .addAttachment("restir_lighting_samples", ITextureFormat.r16f(), CREATE_SAMPLER, this::isRestirEnabled)
                .addAttachment("restir_direct_reservoirs0", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isBlockLightEnabled)
                .addAttachment("restir_indirect_reservoirs0", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirGiEnabled)
                .addAttachment("restir_indirect_reservoirs1", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirGiEnabled)
                .addAttachment("restir_indirect_reservoirs2", ITextureFormat.rgba32f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isRestirGiEnabled)
                .build(this::registerComponent);

        var denoiseFramebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("denoise_result", ITextureFormat.rgba16f(), FLIP | CREATE_SAMPLER | CREATE_PREV_SAMPLER, this::isDenoisingEnabled)
                .build(this::registerComponent);

        var otherFramebuffer = irisFactory.newFramebuffer(properties.getRenderScale())
                .addAttachment("other_handheld", ITextureFormat.rgb16f(), CREATE_SAMPLER, this::isHandheldLightingEnabled)
                .build(this::registerComponent);

        Pipelines.fragData(this, irisFactory, properties.getRenderScale());

        irisFactory.newPipeline()
                .debugGroup("restir")
                .thenFlip(restirFramebuffer)
                .deferredPass("initial direct", restirFramebuffer, "/photonics/rendering/restir/passes/r1_initial_direct.fsh", null, this::isBlockLightEnabled)
                .deferredPass("validate initial direct", restirFramebuffer, "/photonics/rendering/restir/passes/r2_validate_initial_direct.fsh", null, this::isBlockLightEnabled)
                .deferredPass("initial indirect", restirFramebuffer, "/photonics/rendering/restir/passes/r3_initial_indirect.fsh", null, this::isRestirGiEnabled)
                .deferredPass("temporal reuse", restirFramebuffer, "/photonics/rendering/restir/passes/r4_temporal_reuse.fsh", null, this::isRestirEnabled)
                .deferredPass("spatial reuse (setup)", restirFramebuffer, spatialReusePass("setup.fsh"), null, this::isSpatialReuseEnabled)
                .deferredPass("spatial reuse #1", restirFramebuffer, spatialReusePass("pass0.fsh"), null, this::isSpatialReuseEnabled)
                .deferredPass("spatial reuse #2", restirFramebuffer, spatialReusePass("pass1.fsh"), null, this::isSpatialReuseEnabled)
                .deferredPass("spatial reuse #3", restirFramebuffer, spatialReusePass("pass2.fsh"), null, this::isSpatialReuseEnabled)
                .deferredPass("diffuse", restirFramebuffer, "/photonics/rendering/restir/passes/r6_diffuse.fsh", null,this::isRestirEnabled)
                .deferredPass("accumulation", restirFramebuffer, "/photonics/rendering/restir/passes/r7_accumulation.fsh", null, this::isRestirEnabled)

                .debugGroup("svgf")
                .thenRun(() -> atrousIteration = 0)
                .deferredPass("variance prefilter", denoiseFramebuffer, "/photonics/rendering/restir/passes/r8_variance_prefilter.fsh", null, this::isDenoisingEnabled)
                .beginRepeating(denoiserPasses)

                .thenRun(() -> atrousIteration++)
                .thenRun(atrousUpdater::updateNow)
                .thenFlip(denoiseFramebuffer)
                .deferredPass("atrous iteration", denoiseFramebuffer, "/photonics/rendering/restir/passes/r9_denoising.fsh", null, this::isDenoisingEnabled)

                .endRepeating()

                .debugGroup("other")
                .deferredPass("handheld", otherFramebuffer, "/photonics/rendering/restir/passes/r10_handheld.fsh", null, this::isHandheldLightingEnabled)
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

    public boolean isRestirGiEnabled() {
        return properties.isGiEnabled() && properties.useRestirCombinedGi();
    }

    public boolean isRestirEnabled() {
        return isBlockLightEnabled() || isRestirGiEnabled();
    }

    public boolean isSpatialReuseEnabled() {
        return properties.getRestirSpatialReuseSamples() > 0;
    }

    public boolean isHandheldLightingEnabled() {
        return properties.isHandheldLightEnabled();
    }

    public boolean isDenoisingEnabled() {
        return isRestirEnabled() && denoiserPasses > 0;
    }

    private String spatialReusePass(String file) {
        return "/photonics/rendering/restir/passes/spatial_reuse/" + file;
    }
}
