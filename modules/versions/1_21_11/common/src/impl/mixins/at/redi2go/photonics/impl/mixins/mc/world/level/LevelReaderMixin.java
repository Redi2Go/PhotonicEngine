package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.core.IRegistryAccess;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import at.redi2go.photonics.api.mc.world.level.block.IBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
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
