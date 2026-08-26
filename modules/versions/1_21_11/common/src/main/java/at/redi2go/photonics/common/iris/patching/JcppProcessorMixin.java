package at.redi2go.photonics.common.iris.patching;

import at.redi2go.photonics.common.iris.pipeline.UniformPatcher;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.preprocessor.JcppProcessor;
import org.anarres.cpp.Token;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JcppProcessor.class)
public abstract class JcppProcessorMixin {
    @Inject(
            method = "glslPreprocessSource",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                    ordinal = 0
            )
    )
    private static void glslPreprocessSource(
            String source,
            Iterable<StringPair> environmentDefines,
            CallbackInfoReturnable<String> cir,
            @Local(name = "tok") Token tok
    ) {
        UniformPatcher.nextToken(tok);
    }
}
