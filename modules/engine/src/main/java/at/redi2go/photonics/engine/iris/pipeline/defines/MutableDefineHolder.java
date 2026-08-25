package at.redi2go.photonics.engine.iris.pipeline.defines;

import java.util.ArrayList;
import java.util.List;

public class MutableDefineHolder implements IDefineHolder {
    private final List<DefineType> defines = new ArrayList<>();

    public void registerDefines(IDefineHolder defines) {
        this.defines.forEach(e -> e.addTo(defines));
    }

    public void clear() {
        defines.clear();
    }

    @Override
    public void stringDefine(String name, String value) {
        defines.add(new StringDefine(name, value));
    }

    @Override
    public void intDefine(String name, int value) {
        defines.add(new IntDefine(name, value));
    }

    @Override
    public void floatDefine(String name, float value) {
        defines.add(new FloatDefine(name, value));
    }

    @Override
    public void enumDefine(String name, Enum<?> value) {
        defines.add(new EnumDefine(name, value));
    }

    private interface DefineType {
        void addTo(IDefineHolder defineHolder);
    }

    private record StringDefine(String name, String value) implements DefineType {
        @Override
        public void addTo(IDefineHolder defineHolder) {
            defineHolder.stringDefine(name, value);
        }
    }

    private record IntDefine(String name, int value) implements DefineType {
        @Override
        public void addTo(IDefineHolder defineHolder) {
            defineHolder.intDefine(name, value);
        }
    }

    private record FloatDefine(String name, float value) implements DefineType {
        @Override
        public void addTo(IDefineHolder defineHolder) {
            defineHolder.floatDefine(name, value);
        }
    }

    private record EnumDefine(String name, Enum<?> value) implements DefineType {
        @Override
        public void addTo(IDefineHolder defineHolder) {
            defineHolder.enumDefine(name, value);
        }
    }
}
