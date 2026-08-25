package at.redi2go.photonics.engine.iris.properties.impl.types;

import at.redi2go.photonics.engine.iris.pipeline.defines.IDefineHolder;
import at.redi2go.photonics.engine.iris.properties.impl.PropertyType;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class StringPropertyType implements PropertyType<String> {
    public static final String DEFAULT_VALUE = "";
    public static final StringPropertyType INSTANCE = new StringPropertyType();

    private StringPropertyType() {

    }

    @Override
    public String defaultValue(Method method) {
        return DEFAULT_VALUE;
    }

    @Override
    public String parse(String key, String value, Method method, Logger logger) {
        return value;
    }

    @Override
    public boolean validate(String key, String value, Method method, Logger logger) {
        return true;
    }

    @Override
    public void registerDefine(IDefineHolder defineHolder, String key, String value) {
        defineHolder.stringDefine(key, value);
    }
}
