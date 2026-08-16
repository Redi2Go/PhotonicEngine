package at.redi2go.photonics.impl.mixins.mc.core.registries;

import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import at.redi2go.photonics.game.minecraft.core.registries.Registries;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Registries.class)
@SuppressWarnings("unchecked")
public interface RegistriesImpl {
    @Overwrite
    static IHolderLookup<IBlock> block() {
        return (IHolderLookup<IBlock>) BuiltInRegistries.BLOCK;
    }
}
