package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.core.IRegistryAccess;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends ILevelReader {
    @Shadow
    RegistryAccess shadow$registryAccess();

    @Override
    default IRegistryAccess registryAccess() {
        return (IRegistryAccess) shadow$registryAccess();
    }
}
