package at.redi2go.photonics.impl.mixins.shaders.rendering.uniform;

import at.redi2go.photonics.api.shaders.rendering.uniform.IUniformHolder;
import at.redi2go.photonics.api.shaders.rendering.uniform.IUniformUpdateFrequency;
import at.redi2go.photonics.api.shaders.rendering.uniform.IValueUpdateNotifier;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@Mixin(UniformHolder.class)
public interface UniformHolderMixin extends UniformHolder, IUniformHolder {
    @Override
    default IUniformHolder uniform1f(IUniformUpdateFrequency var1, String var2, IntSupplier var3) {
        return (IUniformHolder) uniform1f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform1f(IUniformUpdateFrequency var1, String var2, DoubleSupplier var3) {
        return (IUniformHolder) uniform1f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform1i(IUniformUpdateFrequency var1, String var2, IntSupplier var3) {
        return (IUniformHolder) uniform1i((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform1b(IUniformUpdateFrequency var1, String var2, BooleanSupplier var3) {
        return (IUniformHolder) uniform1b((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform2f(IUniformUpdateFrequency var1, String var2, Supplier<Vector2f> var3) {
        return (IUniformHolder) uniform2f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform2i(IUniformUpdateFrequency var1, String var2, Supplier<Vector2i> var3) {
        return (IUniformHolder) uniform2i((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector3f> var3) {
        return (IUniformHolder) uniform3f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform3i(IUniformUpdateFrequency var1, String var2, Supplier<Vector3i> var3) {
        return (IUniformHolder) uniform3i((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniformTruncated3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3) {
        return (IUniformHolder) uniformTruncated3f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform3d(IUniformUpdateFrequency var1, String var2, Supplier<Vector3d> var3) {
        return (IUniformHolder) uniform3d((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform4f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3) {
        return (IUniformHolder) uniform4f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniform4fArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3) {
        return (IUniformHolder) uniform4fArray((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniformMatrix(IUniformUpdateFrequency var1, String var2, Supplier<Matrix4fc> var3) {
        return (IUniformHolder) uniformMatrix((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IUniformHolder uniformMatrixFromArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3) {
        return (IUniformHolder) uniformMatrixFromArray((UniformUpdateFrequency) (Object) var1, var2, var3);
    }
}
