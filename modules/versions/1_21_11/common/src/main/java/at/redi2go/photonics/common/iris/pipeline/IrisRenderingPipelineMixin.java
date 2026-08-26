package at.redi2go.photonics.common.iris.pipeline;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.pipeline.renderer.composite.PhCompositeRenderer;
import at.redi2go.photonics.engine.iris.IrisManager;
import at.redi2go.photonics.engine.iris.IrisPack;
import at.redi2go.photonics.engine.iris.IrisPackPath;
import at.redi2go.photonics.engine.iris.pipeline.IrisRenderer;
import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDynamicDefines;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.pathways.CenterDepthSampler;
import net.irisshaders.iris.pipeline.CustomTextureManager;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.shaderpack.programs.ComputeSource;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.BufferFlipper;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(IrisRenderingPipeline.class)
public abstract class IrisRenderingPipelineMixin implements IrisRenderingPipelineExt {
    @Shadow private WorldRenderingPhase phase;

    @Shadow @Final private RenderTargets renderTargets;

    @Shadow private ShaderStorageBufferHolder shaderStorageBufferHolder;

    @Shadow @Final private CustomTextureManager customTextureManager;

    @Shadow @Final private FrameUpdateNotifier updateNotifier;

    @Shadow @Final private CenterDepthSampler centerDepthSampler;

    @Shadow @Final private Supplier<ShadowRenderTargets> shadowTargetsSupplier;

    @Shadow @Final private Set<GlImage> customImages;

    @Shadow @Final private CustomUniforms customUniforms;

    @Unique private List<PhCompositeRenderer> phRenderers;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;<init>(Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;Lnet/irisshaders/iris/pipeline/CompositePass;Lnet/irisshaders/iris/shaderpack/properties/PackDirectives;[Lnet/irisshaders/iris/shaderpack/programs/ProgramSource;[[Lnet/irisshaders/iris/shaderpack/programs/ComputeSource;Lnet/irisshaders/iris/targets/RenderTargets;Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;Lnet/irisshaders/iris/gl/texture/TextureAccess;Lnet/irisshaders/iris/uniforms/FrameUpdateNotifier;Lnet/irisshaders/iris/pathways/CenterDepthSampler;Lnet/irisshaders/iris/targets/BufferFlipper;Ljava/util/function/Supplier;Lnet/irisshaders/iris/shaderpack/texture/TextureStage;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Ljava/util/Set;Lcom/google/common/collect/ImmutableMap;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)V",
                    ordinal = 0
            )
    )
    private void init(ProgramSet programSet, CallbackInfo ci, @Local BufferFlipper flipper) {
        phRenderers = List.of();

        Optional<IrisPack> pack = IrisPack.getCurrentPack();

        var renderers = IrisUtil.getPipelineManager().getRenderers();
        var phRenderers = ImmutableList.<PhCompositeRenderer>builder();

        for (var renderer : renderers) {
            var passes = renderer.getPasses();

            var compositeSources = new ProgramSource[passes.size()];
            var computeSources = new ComputeSource[passes.size()][];

            for (int i = 0; i < passes.size(); i++) {
                var pass = passes.get(i);

                try (var dynamicDefines = IrisManager.newDynamicDefines()) {
                    pass.applyBuilder(dynamicDefines);

                    compositeSources[i] = new ProgramSource(
                            pass.hasFragmentShader() ? cleanUpFragmentName(pass.fragmentShader()) : pass.name(),
                            pack.map(e -> e.readFile(IrisPackPath.fromPath(pass.vertexShader()))).orElse(null),
                            null,
                            null,
                            null,
                            pack.map(e -> e.readFile(IrisPackPath.fromPath(pass.fragmentShader()))).orElse(null),
                            programSet,
                            null,
                            null
                    );
                }

                //TODO: For future use
                computeSources[i] = new ComputeSource[0];
            }

            phRenderers.add(
                    new PhCompositeRenderer(
                            renderer.name(),
                            (IrisRenderingPipeline) (Object) this,
                            programSet.getPackDirectives(),
                            compositeSources,
                            computeSources,
                            renderTargets,
                            shaderStorageBufferHolder,
                            customTextureManager.getNoiseTexture(),
                            updateNotifier,
                            centerDepthSampler,
                            flipper,
                            shadowTargetsSupplier,
                            customTextureManager.getCustomTextureIdMap().getOrDefault(TextureStage.DEFERRED, Object2ObjectMaps.emptyMap()),
                            customTextureManager.getIrisCustomTextures(),
                            customImages,
                            customUniforms,
                            passes
                    )
            );
        }

        this.phRenderers = phRenderers.build();
    }

    @Inject(
            method = "beginTranslucents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;renderAll()V"
            )
    )
    public void beginTranslucents(CallbackInfo ci) {
        IrisManager.onRender();
    }

    @Override
    public List<PhCompositeRenderer> getRenderers() {
        return phRenderers;
    }

    @Unique
    private static String cleanUpFragmentName(String fragment) {
        fragment = fragment.substring(fragment.lastIndexOf("/") + 1);
        fragment = fragment.substring(0, fragment.lastIndexOf("."));

        return fragment;
    }
}
