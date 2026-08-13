package at.redi2go.photonics.core.iris.pipeline.uniform;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface IDynamicUniformHolderBuilder<T> {
    T withDynamicUniform(Consumer<IDynamicUniformHolder> consumer);

    default T dynamicUniform1f(IUniformUpdateFrequency var1, String var2, IntSupplier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform1f(var1, var2, var3));
    }

    default T dynamicUniform1f(IUniformUpdateFrequency var1, String var2, DoubleSupplier var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform1f(var1, var2, var3));
    }

    default T dynamicUniform1i(IUniformUpdateFrequency var1, String var2, IntSupplier var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform1i(var1, var2, var3));
    }

    default T dynamicUniform1b(IUniformUpdateFrequency var1, String var2, BooleanSupplier var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform1b(var1, var2, var3));
    }

    default T dynamicUniform2f(IUniformUpdateFrequency var1, String var2, Supplier<Vector2f> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform2f(var1, var2, var3));
    }

    default T dynamicUniform2i(IUniformUpdateFrequency var1, String var2, Supplier<Vector2i> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform2i(var1, var2, var3));
    }

    default T dynamicUniform3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector3f> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform3f(var1, var2, var3));
    }

    default T dynamicUniform3i(IUniformUpdateFrequency var1, String var2, Supplier<Vector3i> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform3i(var1, var2, var3));
    }

    default T dynamicUniformTruncated3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniformTruncated3f(var1, var2, var3));
    }

    default T dynamicUniform3d(IUniformUpdateFrequency var1, String var2, Supplier<Vector3d> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform3d(var1, var2, var3));
    }

    default T dynamicUniform4f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform4f(var1, var2, var3));
    }

    default T dynamicUniform4fArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniform4fArray(var1, var2, var3));
    }

    default T dynamicUniformMatrix(IUniformUpdateFrequency var1, String var2, Supplier<Matrix4fc> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniformMatrix(var1, var2, var3));
    }

    default T dynamicUniformMatrixFromArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3 ) {
        return withDynamicUniform(uniforms -> uniforms.uniformMatrixFromArray(var1, var2, var3));
    }

    default T dynamicUniform1f(String var1, IntSupplier var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform1f(var1, var2, var3));
    }

    default T dynamicUniform1f(String var1, DoubleSupplier var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform1f(var1, var2, var3));
    }

    default T dynamicUniform1i(String var1, IntSupplier var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform1i(var1, var2, var3));
    }

    default T dynamicUniform2f(String var1, Supplier<Vector2f> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform2f(var1, var2, var3));
    }

    default T dynamicUniform2i(String var1, Supplier<Vector2i> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform2i(var1, var2, var3));
    }

    default T dynamicUniform3f(String var1, Supplier<Vector3f> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform3f(var1, var2, var3));
    }

    default T dynamicUniform4f(String var1, Supplier<Vector4f> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform4f(var1, var2, var3));
    }

    default T dynamicUniform4fArray(String var1, Supplier<float[]> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform4fArray(var1, var2, var3));
    }

    default T dynamicUniform4i(String var1, Supplier<Vector4i> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniform4i(var1, var2, var3));
    }

    default T dynamicUniformMatrix(String var1, Supplier<Matrix4fc> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniformMatrix(var1, var2, var3));
    }

    default T dynamicUniformMatrix3(String var1, Supplier<Matrix3fc> var2, IValueUpdateNotifier var3) {
        return withDynamicUniform(uniforms -> uniforms.uniformMatrix3(var1, var2, var3));
    }
}
