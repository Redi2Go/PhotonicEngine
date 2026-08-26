package at.redi2go.photonics.engine.iris.pipeline.uniforms;

import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface IrisUniformHolderBuilder<T> {
    T withUniform(Consumer<IrisUniformHolder> consumer);

    default T uniform1f(IUniformUpdateFrequency var1, String var2, IntSupplier var3) {
        return withUniform(uniforms -> uniforms.uniform1f(var1, var2, var3));
    }

    default T uniform1f(IUniformUpdateFrequency var1, String var2, DoubleSupplier var3 ) {
        return withUniform(uniforms -> uniforms.uniform1f(var1, var2, var3));
    }

    default T uniform1i(IUniformUpdateFrequency var1, String var2, IntSupplier var3 ) {
        return withUniform(uniforms -> uniforms.uniform1i(var1, var2, var3));
    }

    default T uniform1b(IUniformUpdateFrequency var1, String var2, BooleanSupplier var3 ) {
        return withUniform(uniforms -> uniforms.uniform1b(var1, var2, var3));
    }

    default T uniform2f(IUniformUpdateFrequency var1, String var2, Supplier<Vector2f> var3 ) {
        return withUniform(uniforms -> uniforms.uniform2f(var1, var2, var3));
    }

    default T uniform2i(IUniformUpdateFrequency var1, String var2, Supplier<Vector2i> var3 ) {
        return withUniform(uniforms -> uniforms.uniform2i(var1, var2, var3));
    }

    default T uniform3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector3f> var3 ) {
        return withUniform(uniforms -> uniforms.uniform3f(var1, var2, var3));
    }

    default T uniform3i(IUniformUpdateFrequency var1, String var2, Supplier<Vector3i> var3 ) {
        return withUniform(uniforms -> uniforms.uniform3i(var1, var2, var3));
    }

    default T uniformTruncated3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3 ) {
        return withUniform(uniforms -> uniforms.uniformTruncated3f(var1, var2, var3));
    }

    default T uniform3d(IUniformUpdateFrequency var1, String var2, Supplier<Vector3d> var3 ) {
        return withUniform(uniforms -> uniforms.uniform3d(var1, var2, var3));
    }

    default T uniform4f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3 ) {
        return withUniform(uniforms -> uniforms.uniform4f(var1, var2, var3));
    }

    default T uniform4fArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3 ) {
        return withUniform(uniforms -> uniforms.uniform4fArray(var1, var2, var3));
    }

    default T uniformMatrix(IUniformUpdateFrequency var1, String var2, Supplier<Matrix4fc> var3 ) {
        return withUniform(uniforms -> uniforms.uniformMatrix(var1, var2, var3));
    }

    default T uniformMatrixFromArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3 ) {
        return withUniform(uniforms -> uniforms.uniformMatrixFromArray(var1, var2, var3));
    }
}
