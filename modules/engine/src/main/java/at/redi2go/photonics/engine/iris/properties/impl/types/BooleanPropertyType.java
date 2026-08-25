package at.redi2go.photonics.engine.iris.properties.impl.types;

import at.redi2go.photonics.engine.iris.pipeline.DefineHolder;
import at.redi2go.photonics.engine.iris.properties.impl.PropertyType;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class BooleanPropertyType implements PropertyType<Boolean> {
    public static final boolean DEFAULT_VALUE = false;
    public static final BooleanPropertyType INSTANCE = new BooleanPropertyType();

    private BooleanPropertyType() {

    }

    @Override
    public Boolean defaultValue(Method method) {
        return DEFAULT_VALUE;
    }

    @Override
    public Boolean parse(String key, String value, Method method, Logger logger) {
        if ("true".equals(value) || "1".equals(value)) return true;
        if ("false".equals(value) || "0".equals(value)) return false;

        logger.warn("Unexpected value for boolean key {} in shaders.properties: got {}, but expected either true or false", key, value);
        return DEFAULT_VALUE;
    }

    @Override
    public boolean validate(String key, Boolean value, Method method, Logger logger) {
        return true;
    }

    @Override
    public void registerDefine(DefineHolder defineHolder, String key, Boolean value) {
        if (!value) return;

        defineHolder.stringDefine(key, "");
    }
}
