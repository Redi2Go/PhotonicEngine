package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.api.shaders.IPackPath;
import at.redi2go.photonics.common.PatcherBridge;
import at.redi2go.photonics.core.iris.patching.ShaderPatcher;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Mixin(IncludeGraph.class)
public abstract class IncludeGraphMixin {
    @Unique private Path root;
    @Unique private ShaderPatcher patcher;


    @Inject(
            method = "<init>(Ljava/nio/file/Path;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "NEW",
                    target = "()Ljava/util/HashMap;", ordinal = 0
            )
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void init(
            Path root,
            ImmutableList<AbsolutePackPath> startingRefs,
            boolean isZip,
            CallbackInfo ci,
            @Local(argsOnly = true) LocalRef<ImmutableList<AbsolutePackPath>> startingPathsRef
    ) {
        this.root = root;
        this.patcher = PatcherBridge.consumePatcher();

        var newFiles = patcher.getCreatedFiles();

        startingPathsRef.set(
                ImmutableList.<AbsolutePackPath>builderWithExpectedSize(startingRefs.size() + newFiles.size())
                        .addAll(startingRefs)
                        .addAll((List) newFiles)
                        .build()
        );
    }

    @WrapOperation(
            method = "<init>(Ljava/nio/file/Path;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/shaderpack/include/IncludeGraph;readFile(Ljava/nio/file/Path;)Ljava/lang/String;"
            )
    )
    private String readFile(Path path, Operation<String> original) {
        return patcher.readShaderFile(
                IPackPath.fromAbsolutePath("/" + root.relativize(path).toString()),
                packPath -> {
                    var fsPath = packPath.resolved(root);
                    if (!Files.exists(fsPath)) return null;

                    return original.call(fsPath);
                }
        );
    }
}
