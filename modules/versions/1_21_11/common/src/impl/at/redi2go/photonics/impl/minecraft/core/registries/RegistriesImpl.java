package at.redi2go.photonics.impl.minecraft.core.registries;

import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import at.redi2go.photonics.game.minecraft.core.registries.Registries;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Registries.class)
public interface RegistriesImpl {
    @Overwrite
    @SuppressWarnings("unchecked")
    static IHolderLookup<IBlock> block() {
        return (IHolderLookup<IBlock>) BuiltInRegistries.BLOCK;
    }
}
