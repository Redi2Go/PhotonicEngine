package at.redi2go.photonics.impl.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.world.level.IBlockAndTintGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockAndTintGetter.class)
public interface BlockAndTintGetterImpl extends IBlockAndTintGetter {
}
