package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.CompositeRendererExt;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.rendering.opengl.rendering.PhotonicsShader;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pathways.FullScreenQuadRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/irisshaders/iris/pipeline/CompositeRenderer", remap = false)
public class CompositeRendererPassMixin {
   @WrapOperation(method = "renderAll()V", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/framebuffer/GlFramebuffer;bind()V"))
   private void bindFramebuffer(GlFramebuffer instance, Operation<Void> original, @Local(ordinal = 0) int photonicsId) {
      List<PhotonicsShader> shaders = ((CompositeRendererExt)this).photonics$getPhotonicsShaders();
      PhotonicsShader shader = shaders != null && photonicsId >= 0 && photonicsId < shaders.size() ? shaders.get(photonicsId) : null;
      Raytracer.CURRENT_FRAMEBUFFER = null;
      if (shader != null) {
         shader.bindFramebuffer();
      }

      if (Raytracer.CURRENT_FRAMEBUFFER != null) {
         Raytracer.CURRENT_FRAMEBUFFER.bind();
      } else {
         original.call(instance);
      }
   }

   @WrapOperation(method = "renderAll()V", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/gl/program/Program;use()V"))
   private void useProgram(Program instance, Operation<Void> original) {
      original.call(instance);
      Raytracer.bindBuffers(instance.getProgramId());
   }

   @WrapOperation(
      method = "renderAll()V",
      at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pathways/FullScreenQuadRenderer;renderQuad()V")
   )
   private void renderQuad(FullScreenQuadRenderer instance, Operation<Void> original) {
      try {
         original.call(instance);
      } finally {
         if (Raytracer.CURRENT_FRAMEBUFFER != null) {
            Raytracer.CURRENT_FRAMEBUFFER.unbind();
            Raytracer.CURRENT_FRAMEBUFFER = null;
         }
      }
   }
}
