package at.redi2go.photonics.api.mc.core;

import java.util.stream.Stream;

public interface IHolderSet<T> extends Iterable<IHolder<T>> {
    Stream<IHolder<T>> ph$stream();
}
