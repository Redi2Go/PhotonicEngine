package at.redi2go.photonics.engine.iris.pipeline.defines;

import at.redi2go.photonics.game.minecraft.Id;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

public interface IrisDefineHolderBuilder<T> {
    T withDefine(BiConsumer<IrisDefineHolder, Id> consumer);

    T withDefines(List<BiConsumer<IrisDefineHolder, Id>> consumers);

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
