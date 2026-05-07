package at.redi2go.photonics.core.iris.pipeline.uniform;

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

public interface IDynamicUniformHolder extends IUniformHolder {
    IUniformHolder uniform1f(String var1, IntSupplier var2, IValueUpdateNotifier var3);

    IUniformHolder uniform1f(String var1, DoubleSupplier var2, IValueUpdateNotifier var3);

    IUniformHolder uniform1i(String var1, IntSupplier var2, IValueUpdateNotifier var3);

    IUniformHolder uniform2f(String var1, Supplier<Vector2f> var2, IValueUpdateNotifier var3);

    IUniformHolder uniform2i(String var1, Supplier<Vector2i> var2, IValueUpdateNotifier var3);

    IUniformHolder uniform3f(String var1, Supplier<Vector3f> var2, IValueUpdateNotifier var3);

    IUniformHolder uniform4f(String var1, Supplier<Vector4f> var2, IValueUpdateNotifier var3);

    IUniformHolder uniform4fArray(String var1, Supplier<float[]> var2, IValueUpdateNotifier var3);

    IUniformHolder uniform4i(String var1, Supplier<Vector4i> var2, IValueUpdateNotifier var3);

    IUniformHolder uniformMatrix(String var1, Supplier<Matrix4fc> var2, IValueUpdateNotifier var3);

    IUniformHolder uniformMatrix3(String var1, Supplier<Matrix3fc> var2, IValueUpdateNotifier var3);
}
