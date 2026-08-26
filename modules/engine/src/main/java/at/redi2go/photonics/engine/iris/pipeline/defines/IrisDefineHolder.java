package at.redi2go.photonics.engine.iris.pipeline.defines;

public interface IrisDefineHolder {
    void stringDefine(String name, String value);

    void intDefine(String name, int value);

    void floatDefine(String name, float value);

    void enumDefine(String name, Enum<?> value);
}
