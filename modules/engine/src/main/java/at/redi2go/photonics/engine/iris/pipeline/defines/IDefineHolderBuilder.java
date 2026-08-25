package at.redi2go.photonics.engine.iris.pipeline.defines;

import at.redi2go.photonics.game.minecraft.Id;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface IDefineHolderBuilder<T> {
    T withDefine(BiConsumer<IDefineHolder, Id> consumer);

    default T stringDefine(String name, String value) {
        return withDefine((defines, dim) -> defines.stringDefine(name, value));
    }

    default T intDefine(String name, int value) {
        return withDefine((defines, dim) -> defines.intDefine(name, value));
    }

    default T floatDefine(String name, float value) {
        return withDefine((defines, dim) -> defines.floatDefine(name, value));
    }

    default T enumDefine(String name, Enum<?> value) {
        return withDefine((defines, dim) -> defines.enumDefine(name, value));
    }

    default T stringDefine(String name, String value, BooleanSupplier condition) {
        return withDefine((defines, dim) -> {
            if (condition.getAsBoolean())
                defines.stringDefine(name, value);
        });
    }

    default T intDefine(String name, int value, BooleanSupplier condition) {
        return withDefine((defines, dim) -> {
            if (condition.getAsBoolean())
                defines.intDefine(name, value);
        });
    }

    default T floatDefine(String name, float value, BooleanSupplier condition) {
        return withDefine((defines, dim) -> {
            if (condition.getAsBoolean())
                defines.floatDefine(name, value);
        });
    }

    default T enumDefine(String name, Enum<?> value, BooleanSupplier condition) {
        return withDefine((defines, dim) -> {
            if (condition.getAsBoolean())
                defines.enumDefine(name, value);
        });
    }
}
