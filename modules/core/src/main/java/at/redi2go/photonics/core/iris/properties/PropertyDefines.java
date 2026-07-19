package at.redi2go.photonics.core.iris.properties;

import at.redi2go.photonics.core.iris.properties.impl.annotations.Magic;

public interface PropertyDefines {
    @Magic default void stringDefine(String name, String value) {}
    @Magic default void intDefine(String name, int value) {}
    @Magic default void floatDefine(String name, float value) {}
    @Magic default <T extends Enum<T>> void enumDefine(String name, T value) {}

    void defineProperties(PhotonicsProperties properties);
}
