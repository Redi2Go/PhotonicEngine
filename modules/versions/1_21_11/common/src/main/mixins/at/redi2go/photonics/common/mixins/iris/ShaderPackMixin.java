package at.redi2go.photonics.common.mixins.iris;

import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.api.shaders.PhotonicsProperties;
import at.redi2go.photonics.common.PatcherBridge;
import at.redi2go.photonics.core.iris.patching.ShaderPatcher;
import at.redi2go.photonics.impl.shaders.PhotonicsPropertiesImpl;
import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.ShaderPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

@Mixin(ShaderPack.class)
public abstract class ShaderPackMixin implements IShaderPack {
    @Unique private PhotonicsPropertiesImpl phProperties;
    @Unique private ShaderPatcher patcher;

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
        phProperties = new PhotonicsPropertiesImpl();
        var properties = loadShaderProperties(root);

        supportsPhotonics = properties.containsKey(PhotonicsProperties.PHOTONICS_ENABLED_KEY);
        patcher = new ShaderPatcher(this);
        PatcherBridge.PATCHER = patcher;

        if (!supportsPhotonics && patcher.hasPatch())
            phProperties.enabled = Boolean.parseBoolean(
                    changedConfigs.getOrDefault("PHOTONICS_ENABLED", "true")
            );
    }

    @Override
    public String name() {
        return Iris.getIrisConfig()
                .getShaderPackName()
                .orElse("<unknown>");
    }

    @Override
    public boolean supportsPhotonics() {
        return supportsPhotonics;
    }

    @Override
    public PhotonicsProperties properties() {
        return phProperties;
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
}