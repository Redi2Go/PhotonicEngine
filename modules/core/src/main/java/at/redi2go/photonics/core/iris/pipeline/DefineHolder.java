package at.redi2go.photonics.core.iris.pipeline;

public interface DefineHolder {
    void stringDefine(String name, String value);

    void intDefine(String name, int value);

    void floatDefine(String name, float value);

    <T extends Enum<T>> void enumDefine(String name, T value);
}
