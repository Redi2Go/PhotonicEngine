package at.redi2go.photonics.game.minecraft;

import java.util.Optional;

public interface IProperty<T extends Comparable<T>> {
    Optional<T> ph$getValue(String name);
}
