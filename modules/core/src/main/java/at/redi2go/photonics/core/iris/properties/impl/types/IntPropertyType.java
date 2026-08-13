package at.redi2go.photonics.core.iris.properties.impl.types;

import at.redi2go.photonics.core.iris.pipeline.DefineHolder;
import at.redi2go.photonics.core.iris.properties.annotations.IntRange;
import at.redi2go.photonics.core.iris.properties.impl.PropertyType;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class IntPropertyType implements PropertyType<Integer> {
    public static final int DEFAULT_VALUE = 0;
    public static final IntPropertyType INSTANCE = new IntPropertyType();

    private IntPropertyType() {

    }

    @Override
    public Integer defaultValue(Method method) {
        return DEFAULT_VALUE;
    }

    @Override
    public Integer parse(String key, String value, Method method, Logger logger) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Unexpected value for integer key {} in shaders.properties: got {}, but expected an integer", key, value);
            return DEFAULT_VALUE;
        }
    }

    @Override
    public boolean validate(String key, Integer value, Method method, Logger logger) {
        IntRange range = method.getAnnotation(IntRange.class);
        if (range == null) return true;

        if (value >= range.min() && value <= range.max()) return true;

        logger.warn("Unexpected value for integer key {} in shaders.properties: got {}, but expected an integer between {} and {}", key, value, range.min(), range.max());

        return false;
    }

    @Override
    public void registerDefine(DefineHolder defineHolder, String key, Integer value) {
        defineHolder.intDefine(key, value);
    }
}
