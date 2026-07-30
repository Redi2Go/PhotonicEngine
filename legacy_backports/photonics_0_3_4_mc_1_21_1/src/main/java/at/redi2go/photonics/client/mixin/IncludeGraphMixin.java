package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.ShaderPackPath;
import at.redi2go.photonics.client.rendering.patching.Patch;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.FileNode;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IncludeGraph.class, remap = false)
public class IncludeGraphMixin {
   @Inject(
      method = "<init>(Ljava/nio/file/Path;Lcom/google/common/collect/ImmutableList;Z)V",
      at = @At(value = "INVOKE", target = "Ljava/util/HashSet;<init>(Ljava/util/Collection;)V", ordinal = 0, shift = Shift.AFTER)
   )
   private void afterInitArrayList(Path root, ImmutableList<Path> startingPaths, boolean isZip, CallbackInfo ci, @Local List<AbsolutePackPath> queue) {
      try {
         Path path = Path.of(Objects.requireNonNull(this.getClass().getClassLoader().getResource("assets/photonics/shaders")).toURI());

         try (Stream<Path> pathStream = Files.walk(path)) {
            pathStream.forEach(file -> {
               if (Files.isRegularFile(file)) {
                  Path relativePath = path.relativize(file);
                  if (!relativePath.subpath(0, 1).toString().equals("patches")) {
                     queue.add(AbsolutePackPath.fromAbsolutePath("/photonics").resolve(relativePath.toString().replace("\\", "/")));
                  }
               }
            });
         }
      } catch (Exception e) {
         Photonics.error(e);
      }

      Patch patch = Raytracer.getAppliedPatch();
      if (patch != null) {
         for (String createdFile : patch.files) {
            queue.add(AbsolutePackPath.fromAbsolutePath(createdFile.substring("/shaders".length())));
         }
      }
   }

   @Redirect(
      method = "<init>(Ljava/nio/file/Path;Lcom/google/common/collect/ImmutableList;Z)V",
      at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z")
   )
   public boolean contains(Set instance, Object object, @Local FileNode node) {
      return !node.getPath().getPathString().startsWith("/photonics/") ? instance.contains(object) : true;
   }

   @Inject(method = "readFile", at = @At("HEAD"), cancellable = true)
   private static void readFile(Path path, CallbackInfoReturnable<String> cir) throws IOException {
      cir.setReturnValue(Raytracer.readShaderFile(new ShaderPackPath(path), true));
      cir.cancel();
   }
}
