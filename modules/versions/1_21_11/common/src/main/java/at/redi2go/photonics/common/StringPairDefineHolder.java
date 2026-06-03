package at.redi2go.photonics.common;

import at.redi2go.photonics.core.iris.pipeline.DefineHolder;
import net.irisshaders.iris.helpers.StringPair;

import java.util.List;

public record StringPairDefineHolder(List<StringPair> defines) implements DefineHolder {
    @Override
    public void stringDefine(String name, String value) {
        defines.add(new StringPair(name, value));
    }

    @Override
    public void intDefine(String name, int value) {
        defines.add(new StringPair(name, Integer.toString(value)));
    }

    @Override
    public void floatDefine(String name, float value) {
        defines.add(new StringPair(name, Float.toString(value)));
    }

    @Override
    public <T extends Enum<T>> void enumDefine(String name, T value) {
        defines.add(new StringPair(name, Integer.toString(value.ordinal())));
    }
}
