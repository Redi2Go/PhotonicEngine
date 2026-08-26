package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.engine.iris.IrisPack;
import at.redi2go.photonics.engine.iris.IrisPackPath;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

@Mixin(ShaderPack.class)
public abstract class IrisPackImpl implements IrisPack {
    @Shadow @Final private Function<AbsolutePackPath, String> sourceProvider;
    @Unique private String cachedName;
    @Unique private boolean supportsPhotonics = false;

    @Inject(
            method = "<init>(Ljava/nio/file/Path;Ljava/util/Map;Lcom/google/common/collect/ImmutableList;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/EnumMap;<init>(Ljava/lang/Class;)V"
            )
    )
    private void init(Path root, Map<String, String> changedConfigs, ImmutableList<String> environmentDefines, boolean isZip, CallbackInfo ci) {
        supportsPhotonics = loadShaderProperties(root).containsKey("photonics.enabled");
    }

    @Override
    public String ph$name() {
        if (cachedName == null) {
            cachedName = Iris.getIrisConfig()
                    .getShaderPackName()
                    .orElse("<unknown>");
        }

        return cachedName;
    }

    @Override
    public boolean ph$supportsPhotonics() {
        return supportsPhotonics;
    }

    @Override
    public int ph$getBlockId(IBlockState block) {
        return IrisUtil.getBlockId((BlockState) block);
    }

    @Override
    public String readFile(IrisPackPath path) {
        return sourceProvider.apply((AbsolutePackPath) path);
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

    @Mixin(IrisPack.class)
    public interface StaticMethods {
        @Overwrite
        @SuppressWarnings({"unchecked", "rawtypes"})
        static Optional<IrisPack> getCurrentPack() {
            return (Optional) Iris.getCurrentPack();
        }
    }
}
