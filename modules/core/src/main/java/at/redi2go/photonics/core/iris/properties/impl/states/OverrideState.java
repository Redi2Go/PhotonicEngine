package at.redi2go.photonics.core.iris.properties.impl.states;

import at.redi2go.photonics.core.iris.properties.impl.PropertyValue;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class OverrideState {
    private static final Object TEMP = new Object();
    private final Map<Class<?>, Object> instanceLookup;

    private InvocationHandler lastProperty = null;
    private final Map<PropertyValue, Class<?>> overriddenValues = new HashMap<>();

    public OverrideState(
            Map<String, InvocationHandler> magicMethods,
            Map<Class<?>, Object> instanceLookup
    ) {
        this.instanceLookup = instanceLookup;

        magicMethods.put("override", (it, m, args) -> overrideValue(
                        it.getClass().getInterfaces()[0],
                        (Function<?, ?>) args[0],
                        args[1]
                )
        );
    }

    public void clear() {
        lastProperty = null;
        overriddenValues.clear();
    }

    public void setLastProperty(InvocationHandler lastProperty) {
        this.lastProperty = lastProperty;
    }

    @SuppressWarnings("unchecked")
    public Object overrideValue(Class<?> caller, Function<?, ?> optionRaw, Object value) {
        lastProperty = null;
        Function<Object, Object> options = (Function<Object, Object>) optionRaw;

        Class<?> receiverType = probeReceiverType(options);
        options.apply(Objects.requireNonNull(
                instanceLookup.get(receiverType),
                () -> receiverType.getSimpleName() + " is not a properties object"
        ));

        PropertyValue propertyValue = (PropertyValue) Objects.requireNonNull(lastProperty, "Not a property");
        var overriddenBy = overriddenValues.putIfAbsent(propertyValue, caller);

        if (overriddenBy == null || propertyValue.hasValue(value)) {
            propertyValue.setValue(value);
            return null;
        }

        throw new IllegalStateException(propertyValue.getKey() + " was already overridden by " + overriddenBy.getSimpleName());
    }

    private Class<?> probeReceiverType(Function<Object, Object> option) {
        try {
            option.apply(TEMP);
        } catch (ClassCastException e) {
            String className = e.getMessage().split(" ")[7];
            for (var key : instanceLookup.keySet()) {
                if (key.getName().equals(className))
                    return key;
            }
        }

        throw new IllegalStateException("Could not determine function receiver type");
    }
}
