package at.redi2go.photonics.common.mixins.iris.pipeline;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.pipeline.IrisRenderingPipelineExt;
import at.redi2go.photonics.impl.shaders.iris.buffers.GlBufferHolder;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisRenderingPipeline.class)
public abstract class IrisRenderingPipelineMixin implements IrisRenderingPipelineExt {
    @Unique
    private final GlBufferHolder bufferHolder = new GlBufferHolder();

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositeRenderer;<init>(Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;Lnet/irisshaders/iris/pipeline/CompositePass;Lnet/irisshaders/iris/shaderpack/properties/PackDirectives;[Lnet/irisshaders/iris/shaderpack/programs/ProgramSource;[[Lnet/irisshaders/iris/shaderpack/programs/ComputeSource;Lnet/irisshaders/iris/targets/RenderTargets;Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;Lnet/irisshaders/iris/gl/texture/TextureAccess;Lnet/irisshaders/iris/uniforms/FrameUpdateNotifier;Lnet/irisshaders/iris/pathways/CenterDepthSampler;Lnet/irisshaders/iris/targets/BufferFlipper;Ljava/util/function/Supplier;Lnet/irisshaders/iris/shaderpack/texture/TextureStage;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;Ljava/util/Set;Lcom/google/common/collect/ImmutableMap;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)V",
                    ordinal = 0
            )
    )
    private void init(ProgramSet programSet, CallbackInfo ci) {
        IrisUtil.getPhotonics()
                .ifPresent(e -> e.registerBuffers(bufferHolder));
    }

    @Override
    public GlBufferHolder photonics$bufferHolder() {
        return bufferHolder;
    }
}
