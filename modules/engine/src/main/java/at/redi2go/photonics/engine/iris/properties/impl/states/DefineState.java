package at.redi2go.photonics.engine.iris.properties.impl.states;

import at.redi2go.photonics.engine.iris.pipeline.MutableDefineHolder;

import java.lang.reflect.InvocationHandler;
import java.util.Map;

public class DefineState extends MutableDefineHolder {
    public DefineState(Map<String, InvocationHandler> magicMethods) {
        magicMethods.put("stringDefine", (it, m, args) -> stringDefine(args));
        magicMethods.put("intDefine", (it, m, args) -> intDefine(args));
        magicMethods.put("floatDefine", (it, m, args) -> floatDefine(args));
        magicMethods.put("enumDefine", (it, m, args) -> enumDefine(args));
    }

    private Object stringDefine(Object[] args) {
        stringDefine((String) args[0], (String) args[1]);
        return null;
    }

    private Object intDefine(Object[] args) {
        intDefine((String) args[0], (int) args[1]);
        return null;
    }

    private Object floatDefine(Object[] args) {
        floatDefine((String) args[0], (float) args[1]);
        return null;
    }

    private Object enumDefine(Object[] args) {
        enumDefine((String) args[0], (Enum<?>) args[1]);
        return null;
    }
}
