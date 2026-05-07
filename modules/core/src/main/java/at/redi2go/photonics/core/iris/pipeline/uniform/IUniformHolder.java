package at.redi2go.photonics.core.iris.pipeline.uniform;

import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface IUniformHolder {
    IUniformHolder uniform1f(IUniformUpdateFrequency var1, String var2, IntSupplier var3);

    IUniformHolder uniform1f(IUniformUpdateFrequency var1, String var2, DoubleSupplier var3);

    IUniformHolder uniform1i(IUniformUpdateFrequency var1, String var2, IntSupplier var3);

    IUniformHolder uniform1b(IUniformUpdateFrequency var1, String var2, BooleanSupplier var3);

    IUniformHolder uniform2f(IUniformUpdateFrequency var1, String var2, Supplier<Vector2f> var3);

    IUniformHolder uniform2i(IUniformUpdateFrequency var1, String var2, Supplier<Vector2i> var3);

    IUniformHolder uniform3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector3f> var3);

    IUniformHolder uniform3i(IUniformUpdateFrequency var1, String var2, Supplier<Vector3i> var3);

    IUniformHolder uniformTruncated3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3);

    IUniformHolder uniform3d(IUniformUpdateFrequency var1, String var2, Supplier<Vector3d> var3);

    IUniformHolder uniform4f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3);

    IUniformHolder uniform4fArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3);

    IUniformHolder uniformMatrix(IUniformUpdateFrequency var1, String var2, Supplier<Matrix4fc> var3);

    IUniformHolder uniformMatrixFromArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3);
}
