package at.redi2go.photonics.engine.iris.properties;

import at.redi2go.photonics.engine.iris.properties.impl.annotations.Magic;

import java.util.function.Function;

public interface PropertyOverrides {
    @Magic default <O, T> void override(Function<O, T> option, T value) {}

    default void overrideProperties(PhotonicsProperties properties) {

    }
}
