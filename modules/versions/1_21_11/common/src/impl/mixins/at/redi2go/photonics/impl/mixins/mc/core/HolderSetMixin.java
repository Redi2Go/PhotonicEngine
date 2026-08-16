package at.redi2go.photonics.impl.mixins.mc.core;

import at.redi2go.photonics.game.mc.core.IHolder;
import at.redi2go.photonics.game.mc.core.IHolderSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;

@Mixin(HolderSet.class)
public interface HolderSetMixin<T> extends IHolderSet<T> {
    @Shadow
    Stream<Holder<T>> stream();

    @Override
    default Stream<IHolder<T>> ph$stream() {
        return (Stream) stream();
    }
}
