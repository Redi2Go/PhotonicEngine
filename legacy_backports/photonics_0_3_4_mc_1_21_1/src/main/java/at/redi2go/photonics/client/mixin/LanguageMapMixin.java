package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.ShaderPackPath;
import at.redi2go.photonics.client.rendering.patching.Patch;
import com.llamalad7.mixinextras.sugar.Local;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Properties;
import net.irisshaders.iris.shaderpack.LanguageMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LanguageMap.class, remap = false)
public class LanguageMapMixin {
   @Redirect(method = "lambda$new$2", at = @At(value = "INVOKE", target = "Ljava/util/Properties;load(Ljava/io/Reader;)V"))
   public void loadProperties(Properties instance, Reader reader, @Local(argsOnly = true) Path path) throws IOException {
      Patch patch = Raytracer.getAppliedPatch();
      if (patch != null) {
         String patchedFile = patch.readPatchedFile(new ShaderPackPath(path));
         if (patchedFile != null) {
            instance.load(new StringReader(patchedFile));
            return;
         }
      }

      instance.load(reader);
   }
}
