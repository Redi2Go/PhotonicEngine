package at.redi2go.photonics.core.iris.pipeline;

public interface DefineHolder {
    void stringDefine(String name, String value);

    void intDefine(String name, int value);

    void floatDefine(String name, float value);

    void enumDefine(String name, Enum<?> value);
}
