package at.redi2go.photonics.client.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import net.irisshaders.iris.shaderpack.loading.ProgramGroup;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Unique
@Mixin(value = ProgramId.class, remap = false)
public class ProgramIdMixin {
   @Shadow
   @Final
   @Mutable
   private static ProgramId[] $VALUES;
   private static final ProgramId VOXELS = addVariant("Voxels", ProgramGroup.Gbuffers, "voxels");
   private static final ProgramId SHADOW_VOXELS = addVariant("ShadowVoxels", ProgramGroup.Shadow, "voxels");

   @Invoker("<init>")
   private static ProgramId invokeInit(String internalName, int internalId, ProgramGroup group, String name) {
      throw new AssertionError();
   }

   @Unique
   private static ProgramId addVariant(String internalName, ProgramGroup group, String name) {
      ArrayList<ProgramId> variants = new ArrayList<>(Arrays.asList($VALUES));
      ProgramId instrument = invokeInit(internalName, variants.getLast().ordinal() + 1, group, name);
      variants.add(instrument);
      $VALUES = variants.toArray(new ProgramId[0]);
      return instrument;
   }
}
