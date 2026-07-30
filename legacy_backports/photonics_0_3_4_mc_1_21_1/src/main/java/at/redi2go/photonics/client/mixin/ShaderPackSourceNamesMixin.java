package at.redi2go.photonics.client.mixin;

import com.google.common.collect.ImmutableList.Builder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.shaderpack.include.ShaderPackSourceNames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderPackSourceNames.class)
public class ShaderPackSourceNamesMixin {
   @WrapOperation(
      method = "findPotentialStarts",
      at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;builder()Lcom/google/common/collect/ImmutableList$Builder;")
   )
   private static Builder<String> findPotentialStarts(Operation<Builder<String>> original) {
      return ((Builder)original.call(new Object[0])).add("ph_lights.json");
   }
}
