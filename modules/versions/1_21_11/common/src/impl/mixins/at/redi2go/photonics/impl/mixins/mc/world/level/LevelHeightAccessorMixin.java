package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.world.level.ILevelHeightAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelHeightAccessor.class)
public interface LevelHeightAccessorMixin extends ILevelHeightAccessor{
    @Shadow
    int getSectionIndexFromSectionY(int i);

    @Override
    default int ph$getSectionIndexFromSectionY(int sectionY) {
        return getSectionIndexFromSectionY(sectionY);
    }
}
