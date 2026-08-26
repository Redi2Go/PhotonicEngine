package at.redi2go.photonics.common.iris.pipeline.defines;

import at.redi2go.photonics.engine.iris.pipeline.defines.IrisDefineHolder;
import net.irisshaders.iris.helpers.StringPair;

import java.util.List;

public record StringPairDefineHolder(List<StringPair> defines) implements IrisDefineHolder {
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
    public void enumDefine(String name, Enum<?> value) {
        defines.add(new StringPair(name, Integer.toString(value.ordinal())));
    }
}
