package at.redi2go.photonics.common.iris.pipeline.buffers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pipeline.FinalPassRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FinalPassRenderer.class)
public abstract class FinalPassRendererMixin {
    @Shadow
    @Final
    private WorldRenderingPipeline pipeline;

    @WrapOperation(
            method = "renderFinalPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/ComputeProgram;use()V"
            )
    )
    public void useCompute(ComputeProgram instance, Operation<Void> original) {
        IrisBuffers.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }

    @WrapOperation(
            method = "renderFinalPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/Program;use()V"
            )
    )
    public void useCompute(Program instance, Operation<Void> original) {
        IrisBuffers.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }
}
