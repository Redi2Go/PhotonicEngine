package at.redi2go.photonics.engine.iris.pipeline.uniforms;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface IrisDynamicUniformHolder extends IrisUniformHolder {
    IrisUniformHolder uniform1f(String var1, IntSupplier var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform1f(String var1, DoubleSupplier var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform1i(String var1, IntSupplier var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform2f(String var1, Supplier<Vector2f> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform2i(String var1, Supplier<Vector2i> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform3f(String var1, Supplier<Vector3f> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform4f(String var1, Supplier<Vector4f> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform4fArray(String var1, Supplier<float[]> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniform4i(String var1, Supplier<Vector4i> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniformMatrix(String var1, Supplier<Matrix4fc> var2, IValueUpdateNotifier var3);

    IrisUniformHolder uniformMatrix3(String var1, Supplier<Matrix3fc> var2, IValueUpdateNotifier var3);
}
