package at.redi2go.photonics.common.mixins.iris.pipeline.passes.composite;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.pipeline.CompositeRendererPassExt;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.InternalIrisFramebuffer;
import at.redi2go.photonics.common.iris.pipeline.renderer.PhotonicsRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderPass;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pipeline.CompositePass;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompositeRenderer.class)
public abstract class CompositeRendererMixin {
    @Shadow
    @Final
    private WorldRenderingPipeline pipeline;

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositePass;name()Ljava/lang/String;"
            )
    )
    private String replaceName(CompositePass instance, Operation<String> original) {
        return (Object) this instanceof PhotonicsRenderer renderer ? renderer.getName() : original.call(instance);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/ComputeProgram;use()V"
            )
    )
    private void useCompute(ComputeProgram instance, Operation<Void> original) {
        IrisUtil.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/Program;use()V"
            )
    )
    private void use(Program instance, Operation<Void> original) {
        IrisUtil.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"
            )
    )
    public void renderAll(
            RenderPass instance,
            int i,
            int j,
            int k,
            int l,
            Operation<Void> original
    ) {
        original.call(instance, i, j, k, l);
        ((CompositeRendererPassExt) instance.iris$getCustomPass())
                .getFramebuffer()
                .ifPresent(e -> ((InternalIrisFramebuffer) e).unbind());
    }
}
