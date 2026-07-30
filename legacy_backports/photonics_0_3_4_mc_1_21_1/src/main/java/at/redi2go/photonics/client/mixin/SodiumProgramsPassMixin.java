package at.redi2go.photonics.client.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms.Pass;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Pass.class, remap = false)
public class SodiumProgramsPassMixin {
   @Shadow
   @Final
   @Mutable
   private static Pass[] $VALUES;
   private static final Pass VOXELS = addVariant("VOXELS", ProgramId.valueOf("Voxels"));
   private static final Pass SHADOW_VOXELS = addVariant("SHADOW_VOXELS", ProgramId.valueOf("ShadowVoxels"));

   @Invoker("<init>")
   private static Pass invokeInit(String internalName, int internalId, ProgramId originalId) {
      throw new AssertionError();
   }

   @Unique
   private static Pass addVariant(String internalName, ProgramId originalId) {
      ArrayList<Pass> variants = new ArrayList<>(Arrays.asList($VALUES));
      Pass instrument = invokeInit(internalName, variants.getLast().ordinal() + 1, originalId);
      variants.add(instrument);
      $VALUES = variants.toArray(new Pass[0]);
      return instrument;
   }
}
