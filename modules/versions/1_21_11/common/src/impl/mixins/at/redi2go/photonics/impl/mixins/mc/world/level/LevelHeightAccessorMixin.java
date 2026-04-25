package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.world.level.ILevelHeightAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelHeightAccessor.class)
public interface LevelHeightAccessorMixin extends ILevelHeightAccessor {
}
