package at.redi2go.photonics.core.iris.properties.impl;

import at.redi2go.photonics.core.iris.properties.annotations.Key;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

public class PropertyValue implements InvocationHandler {
    private final String key;
    private Object value;
    
    public PropertyValue(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }

    public boolean hasValue(Object value) {
        return Objects.equals(this.value, value);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValueFrom(
            Method method,
            @Nullable Key requestedKey,
            PropertyType<?> type,
            Properties properties,
            Logger logger
    ) {
        value = type.extractValue(
                key,
                getProperty(requestedKey, properties),
                method,
                logger
        );
    }

    private @Nullable String getProperty(@Nullable Key requestedKey, Properties properties) {
        String value = properties.getProperty(key);
        if (value != null || requestedKey == null) return value;

        String[] legacyKeys = requestedKey.legacy();
        for (var legacyKey : legacyKeys) {
            if (legacyKey.isBlank()) continue;

            value = properties.getProperty(legacyKey.trim());
            if (value != null) break;
        }

        return value;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return value;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PropertyValue other
                && key.equals(other.key);
    }
}
