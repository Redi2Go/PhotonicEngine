package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.game.mc.world.level.IBlockState;
import at.redi2go.photonics.core.iris.IrisManager;
import at.redi2go.photonics.core.iris.IrisPack;
import at.redi2go.photonics.common.StringPairDefineHolder;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.UniformPatcher;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Mixin(ShaderPack.class)
public abstract class ShaderPackMixin implements IrisPack {
    @Unique private boolean supportsPhotonics = false;

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/EnumMap;<init>(Ljava/lang/Class;)V"
            )
    )
    private void init(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<String> environmentDefines,
            boolean isZip,
            CallbackInfo ci
    ) {
        var properties = loadShaderProperties(root);

        supportsPhotonics = properties.containsKey("photonics.enabled");
        IrisManager.setupShaderPatcher(this, Boolean.parseBoolean(changedConfigs.getOrDefault("PHOTONICS_ENABLED", "true")));
    }

    @Override
    public String ph$name() {
        return Iris.getIrisConfig()
                .getShaderPackName()
                .orElse("<unknown>");
    }

    @Override
    public int ph$getBlockId(IBlockState block) {
        return IrisUtil.getBlockId((BlockState) block);
    }

    @Override
    public boolean ph$supportsPhotonics() {
        return supportsPhotonics;
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
    private void addRegularDefines(
            Path root,
            Map<String, String> changedConfigs,
            ImmutableList<StringPair> environmentDefines,
            boolean isZip,
            CallbackInfo ci,
            @Local(name = "newEnvDefines") List<StringPair> newEnvDefines
    ) {
        // These are the defines used by the preprocessor for everything else
        IrisManager.registerDefines(new StringPairDefineHolder(newEnvDefines));
    }

    @Unique
    private static boolean isPhotonicsSource = false;

    @Inject(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;",
                    shift = At.Shift.BEFORE
            )
    )
    private static void dimensionDefinesPre(List disabledPrograms, IncludeProcessor includeProcessor, Iterable finalEnvironmentDefines1, AbsolutePackPath path, CallbackInfoReturnable<String> cir) {
        isPhotonicsSource = path.getPathString().contains("photonics");
    }

    @ModifyArgs(
            method = "lambda$new$8",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"
            )
    )
    private static void dimensionDefines(
            Args args
    ) {
        if (!isPhotonicsSource)
            return;

        Iterable<StringPair> environmentDefines = args.get(1);

        List<StringPair> definitions = Lists.newArrayList(environmentDefines.iterator());

        String dimensionDefine = switch (Iris.getCurrentDimension().getName()) {
            case "the_nether" -> "NETHER";
            case "the_end" -> "END";

            default -> "OVERWORLD";
        };

        stringDefine(definitions, dimensionDefine, "");

        definitions.add(new StringPair(dimensionDefine, ""));

        args.set(1, definitions);
    }


    @Unique
    private static Properties loadShaderProperties(Path shaderPath) {
        var properties = new Properties();

        try (var reader = Files.newBufferedReader(shaderPath.resolve("shaders.properties"))) {
            properties.load(reader);
        } catch (IOException e) {
            // Ignored
        }

        return properties;
    }

    @Unique
    private static void stringDefine(List<StringPair> defines, String name, String value) {
        defines.add(new StringPair(name, value));
    }

    @Unique
    private static void intDefine(List<StringPair> defines, String name, int value) {
        defines.add(new StringPair(name, Integer.toString(value)));
    }

    @Unique
    private static void floatDefine(List<StringPair> defines, String name, float value) {
        defines.add(new StringPair(name, Float.toString(value)));
    }

    @Unique
    private static <T extends Enum<T>> void enumDefine(List<StringPair> defines, String name, T value) {
        defines.add(new StringPair(name, Integer.toString(value.ordinal())));
    }
}
