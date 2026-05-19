package at.redi2go.photonics.common.mixins.iris;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.shaderpack.include.ShaderPackSourceNames;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderPackSourceNames.class)
public abstract class ShaderPackSourceNamesMixin {
    @Inject(
            method = "findPotentialStarts",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;"
            )
    )
    private static void findPotentialStarts(
            CallbackInfoReturnable<ImmutableList<String>> cir,
            @Local ImmutableList.Builder<String> potentialFileNames
    ) {
        potentialFileNames.add("ph_lights.json");
    }
}
