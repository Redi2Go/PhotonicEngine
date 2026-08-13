package at.redi2go.photonics.core.iris.properties.impl.types;

import at.redi2go.photonics.core.iris.pipeline.DefineHolder;
import at.redi2go.photonics.core.iris.properties.annotations.FloatRange;
import at.redi2go.photonics.core.iris.properties.impl.PropertyType;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class FloatPropertyType implements PropertyType<Float> {
    public static final float DEFAULT_VALUE = 0.0f;
    public static final FloatPropertyType INSTANCE = new FloatPropertyType();

    private FloatPropertyType() {

    }

    @Override
    public Float defaultValue(Method method) {
        return DEFAULT_VALUE;
    }

    @Override
    public Float parse(String key, String value, Method method, Logger logger) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            logger.warn("Unexpected value for float key {} in shaders.properties: got {}, but expected a float", key, value);
            return DEFAULT_VALUE;
        }
    }

    @Override
    public boolean validate(String key, Float value, Method method, Logger logger) {
        FloatRange range = method.getAnnotation(FloatRange.class);
        if (range == null) return true;

        if (value >= range.min() && value <= range.max()) return true;

        logger.warn("Unexpected value for float key {} in shaders.properties: got {}, but expected a float between {} and {}", key, value, range.min(), range.max());

        return false;
    }

    @Override
    public void registerDefine(DefineHolder defineHolder, String key, Float value) {
        defineHolder.floatDefine(key, value);
    }
}
