package at.redi2go.photonics.impl.minecraft.core;

import at.redi2go.photonics.game.minecraft.core.IHolder;
import at.redi2go.photonics.game.minecraft.core.IHolderSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;

@Mixin(HolderSet.class)
public interface HolderSetImpl<T> extends IHolderSet<T> {
    @Shadow
    Stream<Holder<T>> stream();

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Stream<IHolder<T>> ph$stream() {
        return (Stream) stream();
    }
}
