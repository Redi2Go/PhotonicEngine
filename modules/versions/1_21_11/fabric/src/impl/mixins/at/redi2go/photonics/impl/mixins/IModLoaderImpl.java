package at.redi2go.photonics.impl.mixins;

import at.redi2go.photonics.api.ModLoader;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.file.Path;

@Mixin(value = ModLoader.class)
public interface IModLoaderImpl {
    @Overwrite
    static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Overwrite
    static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
