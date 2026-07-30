package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.rendering.opengl.rendering.ShaderUtil;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = ProgramBuilder.class, remap = false)
public class ProgramBuilderMixin {
   @ModifyArgs(
      method = "buildShader",
      at = @At(
         value = "INVOKE",
         target = "Lnet/irisshaders/iris/gl/shader/GlShader;<init>(Lnet/irisshaders/iris/gl/shader/ShaderType;Ljava/lang/String;Ljava/lang/String;)V"
      )
   )
   private static void initGlShader(Args args) {
      if (((String)args.get(1)).contains("ph_")) {
         args.set(2, ShaderUtil.preprocessAutoUniforms((String)args.get(2)));
      }
   }
}
