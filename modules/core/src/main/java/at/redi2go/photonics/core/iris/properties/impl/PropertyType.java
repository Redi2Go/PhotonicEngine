package at.redi2go.photonics.core.iris.properties.impl;

import at.redi2go.photonics.core.iris.pipeline.DefineHolder;
import at.redi2go.photonics.core.iris.properties.annotations.DefaultValue;
import at.redi2go.photonics.core.iris.properties.annotations.Property;
import at.redi2go.photonics.core.iris.properties.impl.types.BooleanPropertyType;
import at.redi2go.photonics.core.iris.properties.impl.types.EnumPropertyType;
import at.redi2go.photonics.core.iris.properties.impl.types.FloatPropertyType;
import at.redi2go.photonics.core.iris.properties.impl.types.IntPropertyType;
import at.redi2go.photonics.core.iris.properties.impl.types.StringPropertyType;
import com.google.gson.internal.Primitives;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

public interface PropertyType<T> {
    Map<Class<?>, PropertyType<?>> TYPES = Map.of(
         Boolean.class, BooleanPropertyType.INSTANCE,
         Integer.class, IntPropertyType.INSTANCE,
         Float.class, FloatPropertyType.INSTANCE,
         String.class, StringPropertyType.INSTANCE,
         Enum.class, EnumPropertyType.INSTANCE
    );

    T defaultValue(Method method);

    T parse(String key, String value, Method method, Logger logger);

    boolean validate(String key, T value, Method method, Logger logger);

    void registerDefine(DefineHolder defineHolder, String key, T value);

    default T extractValue(String key, @Nullable String value, Method method, Logger logger) {
        value = value != null ? value : getDefaultValue(method);

        if (value != null) {
            T parsedValue = parse(key, value, method, logger);
            if (validate(key, parsedValue, method, logger))
                return parsedValue;
        }

        return defaultValue(method);
    }

    static @Nullable String getDefaultValue(Method method) {
        DefaultValue defaultValue = method.getAnnotation(DefaultValue.class);
        return defaultValue == null ? null : defaultValue.value();
    }

    static @Nullable PropertyType<?> getPropertyType(Class<?> clazz) {
        clazz = Primitives.wrap(clazz);

        for (var entry : TYPES.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz))
                return entry.getValue();
        }

        return null;
    }
}
