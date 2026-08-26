package at.redi2go.photonics.engine.iris.properties.impl;

import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolder;
import at.redi2go.photonics.engine.iris.properties.PhotonicsProperties;
import at.redi2go.photonics.engine.iris.properties.PropertyDefines;
import at.redi2go.photonics.engine.iris.properties.PropertyOverrides;
import at.redi2go.photonics.engine.iris.properties.annotations.Defines;
import at.redi2go.photonics.engine.iris.properties.annotations.Key;
import at.redi2go.photonics.engine.iris.properties.impl.annotations.Magic;
import at.redi2go.photonics.engine.iris.properties.impl.states.DefineState;
import at.redi2go.photonics.engine.iris.properties.impl.states.OverrideState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

public class PropertiesManager implements InvocationHandler {
    private static final String[] IGNORED_PREFIXES = new String[]{"use", "is", "get"};
    private static final String[] IGNORED_SUFFIXES = new String[]{"properties"};

    private boolean forceEnabled = false;
    private final Map<String, InvocationHandler> magicMethods = new HashMap<>();

    private final Set<String> keys = new HashSet<>();
    private final Map<Method, InvocationHandler> methodLookup = new HashMap<>();
    private final Map<Class<?>, Object> instanceLookup = new HashMap<>();

    private final DefineState defineState = new DefineState(magicMethods);
    private final OverrideState overrideState = new OverrideState(magicMethods, instanceLookup);

    public void registerDefines(IrisDefineHolder defineHolder) {
        defineState.registerDefines(defineHolder);
    }

    public void setForceEnabled(boolean forceEnabled) {
        this.forceEnabled = forceEnabled;
    }

    public void setProperties(@Nullable Properties properties, Logger logger) {
        keys.clear();
        methodLookup.clear();
        instanceLookup.clear();

        defineState.clear();
        overrideState.clear();

        if (properties == null) return;
        if (forceEnabled) properties.setProperty("photonics.enabled", "true");

        PhotonicsProperties phProperties = (PhotonicsProperties) loadPropertyObject("photonics", PhotonicsProperties.class, properties, logger);
        if (phProperties.isEnabled()) {
            var renderer = phProperties.getRenderer();
            loadPropertyObject("photonics." + renderer.getKey(), renderer.getPropertiesType(), properties, logger);
        }

        for (var obj : instanceLookup.values()) {
            if (obj instanceof PropertyDefines defines)
                defines.defineProperties(phProperties);

            if (obj instanceof PropertyOverrides overrides)
                overrides.overrideProperties(phProperties);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperties(Class<T> type) {
        return (T) instanceLookup.get(type);
    }

    private Object loadPropertyObject(String prefix, Class<?> clazz, Properties properties, Logger logger) {
        if (!clazz.isInterface()) throw new IllegalArgumentException(clazz.getSimpleName() + " must be an interface");
        if (instanceLookup.containsKey(clazz)) return instanceLookup.get(clazz);

        Queue<Class<?>> queue = new ArrayDeque<>(List.of(clazz));
        Set<Class<?>> visited = new HashSet<>();

        Map<MethodSignature, InvocationHandler> signatureValueMapping = new HashMap<>();

        while (!queue.isEmpty()) {
            Class<?> type = queue.remove();
            if (!visited.add(type)) continue;

            for (var method : type.getDeclaredMethods()) {
                methodLookup.put(
                        method,
                        signatureValueMapping.computeIfAbsent(
                                new MethodSignature(method),
                                signature -> {
                                    Method m = signature.method();

                                    if (m.getDeclaredAnnotation(Magic.class) != null) {
                                        return Objects.requireNonNull(
                                                magicMethods.get(m.getName()),
                                                () -> "Magic method '" + m.getName() + "' does not exist"
                                        );
                                    } else if (m.isDefault()) {
                                        return InvocationHandler::invokeDefault;
                                    } else {
                                        return processMethod(prefix, m, properties, logger);
                                    }
                                }
                        )
                );
            }

            for (var superclass : type.getInterfaces()) {
                queue.offer(superclass);
            }
        }

        return instanceLookup.computeIfAbsent(clazz, (it) -> Proxy.newProxyInstance(
                it.getClassLoader(),
                new Class[]{it},
                this
        ));
    }

    private InvocationHandler processMethod(String prefix, Method method, Properties properties, Logger logger) {
        Class<?> returnType = method.getReturnType();
        PropertyType<?> type = PropertyType.getPropertyType(returnType);
        if (type != null) return processProperty(prefix, method, type, properties, logger);

        if (!returnType.isInterface())
            throw new IllegalStateException("cannot parse property type of " + returnType.getSimpleName());

        Key requestedKey = method.getDeclaredAnnotation(Key.class);
        String key = getBaseKey(method, requestedKey);

        Object result = loadPropertyObject(prefix + "." + key, returnType, properties, logger);

        return new PropertyObject(result);
    }

    @SuppressWarnings("unchecked")
    private InvocationHandler processProperty(
            String prefix,
            Method method,
            PropertyType<?> type,
            Properties properties,
            Logger logger
    ) {
        Key requestedKey = method.getDeclaredAnnotation(Key.class);

        PropertyValue value = new PropertyValue(prefix + "." + getBaseKey(method, requestedKey));
        value.setValueFrom(method, requestedKey, type, properties, logger);

        if (!keys.add(value.getKey()))
            throw new IllegalStateException("Duplicate property: " + value.getKey());

        Defines defines = method.getDeclaredAnnotation(Defines.class);
        if (defines != null)
            ((PropertyType<Object>) type).registerDefine(defineState, defines.value(), value.getValue());

        return value;
    }

    @Override
    public Object invoke(
            Object proxy,
            Method method,
            Object[] args
    ) throws Throwable {
        InvocationHandler body = methodLookup.get(method);
        if (body.getClass() == PropertyValue.class)
            overrideState.setLastProperty(body);

        return body.invoke(proxy, method, args);
    }

    private record MethodSignature(Method method) {
        @Override
        public int hashCode() {
            int hash = method.getName().hashCode();

            hash = hash * 31 + method.getReturnType().hashCode();
            hash = hash * 31 + method.getParameterCount();

            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof MethodSignature other)) return false;

            return method.getName().equals(other.method.getName()) &&
                    method.getReturnType().equals(other.method.getReturnType()) &&
                    Arrays.equals(method.getParameterTypes(), other.method.getParameterTypes());
        }
    }

    private record PropertyObject(Object value) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return value;
        }
    }

    private static String getBaseKey(Method method, @Nullable Key requestedKey) {
        if (requestedKey == null || requestedKey.value().isBlank())
            return getKeyPart(method.getName());

        return requestedKey.prefix().isBlank() ?
                requestedKey.value() :
                requestedKey.prefix().trim() + "." + requestedKey.value();
    }

    private static String getKeyPart(String name) {
        int start = 0;
        int end = name.length();

        for (var prefix : IGNORED_PREFIXES) {
            if (!name.regionMatches(true, 0, prefix, 0, prefix.length())) continue;

            start = prefix.length();
            break;
        }

        for (var suffix : IGNORED_SUFFIXES) {
            int offset = name.length() - suffix.length();
            if (!name.regionMatches(true, offset, suffix, 0, suffix.length())) continue;

            end = offset;
            break;
        }

        String namePart = name.substring(start + 1, end);
        return String.valueOf(name.charAt(start)).toLowerCase() + namePart;
    }
}
