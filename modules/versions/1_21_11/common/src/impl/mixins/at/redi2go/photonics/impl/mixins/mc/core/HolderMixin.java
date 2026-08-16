package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.game.minecraft.core.IHolder;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Holder.class)
public interface HolderMixin<T> extends IHolder<T> {
    @Shadow
    T value();

    @Override
    default T ph$value() {
        return value();
    }
}
