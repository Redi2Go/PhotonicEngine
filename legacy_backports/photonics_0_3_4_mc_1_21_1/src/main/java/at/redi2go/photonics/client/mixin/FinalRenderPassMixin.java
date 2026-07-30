package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pipeline.FinalPassRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FinalPassRenderer.class)
public class FinalRenderPassMixin {
   @WrapOperation(method = "renderFinalPass", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/ComputeProgram;use()V"))
   public void useCompute(ComputeProgram instance, Operation<Void> original) {
      Raytracer.bindBuffers(instance.getProgramId());
      original.call(new Object[]{instance});
   }

   @WrapOperation(method = "renderFinalPass", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/Program;use()V"))
   public void useCompute(Program instance, Operation<Void> original) {
      Raytracer.bindBuffers(instance.getProgramId());
      original.call(new Object[]{instance});
   }
}
