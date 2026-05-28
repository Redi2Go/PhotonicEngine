package at.redi2go.photonics.api.mc;

import java.util.Optional;

public interface IProperty<T extends Comparable<T>> {
    Optional<T> ph$getValue(String name);
}
