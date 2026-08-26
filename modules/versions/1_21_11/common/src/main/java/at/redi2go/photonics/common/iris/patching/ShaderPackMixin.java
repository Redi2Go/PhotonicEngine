package at.redi2go.photonics.common.iris.patching;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.pipeline.UniformPatcher;
import at.redi2go.photonics.common.iris.pipeline.defines.StringPairDefineHolder;
import at.redi2go.photonics.engine.iris.IrisManager;
import at.redi2go.photonics.engine.iris.IrisPack;
import at.redi2go.photonics.mixins.MixinOrder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ShaderPack.class)
public abstract class ShaderPackMixin {
    @Unique private static boolean isPhotonicsSource = false;

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/EnumMap;<init>(Ljava/lang/Class;)V"
            ),
            order = MixinOrder.LATE
    )
    private void init(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<String> environmentDefines,
            boolean isZip,
            CallbackInfo ci
    ) {
        IrisManager.prepareShaderPatcher((IrisPack) this, Boolean.parseBoolean(changedConfigs.getOrDefault("PHOTONICS_ENABLED", "true")));
    }

    @WrapOperation(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"
            ),
            remap = false
    )
    private static String lambda$new$8Post(
            String source,
            Iterable<StringPair> environmentDefines,
            Operation<String> original
    ) {
        UniformPatcher.prepare();
        try {
            return UniformPatcher.addRequiredUniforms(original.call(source, environmentDefines));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Inject(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"
            )
    )
    private static void addDynamicDefinesPre(
            List<String> disabledPrograms,
            IncludeProcessor includeProcessor,
            Iterable<StringPair> finalEnvironmentDefines1,
            AbsolutePackPath path,
            CallbackInfoReturnable<String> cir
    ) {
        isPhotonicsSource = path.getPathString().startsWith("/photonics");
    }

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
                    ordinal = 0
            )
    )
    private void addEnvironmentalDefines(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<StringPair> environmentDefines,
            boolean isZip,
            CallbackInfo ci,
            @Local(name = "envDefines1") ArrayList<StringPair> defines
    ) {
        // These are the defines used by the preprocessor for shaders.properties
        IrisManager.registerVersionDefines(new StringPairDefineHolder(defines));
    }

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList;copyOf(Ljava/util/Collection;)Lcom/google/common/collect/ImmutableList;",
                    ordinal = 1
            )
    )
    private void addStaticDefines(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<StringPair> environmentDefines,
            boolean isZip,
            CallbackInfo ci,
            @Local(name = "newEnvDefines") List<StringPair> newEnvDefines
    ) {
        // These are the defines used by the preprocessor for everything else
        IrisManager.registerStaticDefines(new StringPairDefineHolder(newEnvDefines));
    }

    @ModifyArgs(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"
            )
    )
    private static void addDynamicDefines(Args args) {
        if (!isPhotonicsSource) return;

        Iterable<StringPair> environmentDefines = args.get(1);
        List<StringPair> defines = Lists.newArrayList(environmentDefines.iterator());

        IrisManager.registerDynamicDefines(
                new StringPairDefineHolder(defines),
                IrisUtil.getCurrentDimension()
        );

        args.set(1, defines);
    }
}
