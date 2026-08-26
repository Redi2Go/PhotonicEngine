package at.redi2go.photonics.common.iris.pipeline.uniforms;

import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IUniformUpdateFrequency;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@Mixin(UniformHolder.class)
@SuppressWarnings("DataFlowIssue")
public interface IrisUniformHolderImpl extends UniformHolder, IrisUniformHolder {
    @Override
    default IrisUniformHolder uniform1f(IUniformUpdateFrequency var1, String var2, IntSupplier var3) {
        return (IrisUniformHolder) uniform1f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform1f(IUniformUpdateFrequency var1, String var2, DoubleSupplier var3) {
        return (IrisUniformHolder) uniform1f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform1i(IUniformUpdateFrequency var1, String var2, IntSupplier var3) {
        return (IrisUniformHolder) uniform1i((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform1b(IUniformUpdateFrequency var1, String var2, BooleanSupplier var3) {
        return (IrisUniformHolder) uniform1b((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform2f(IUniformUpdateFrequency var1, String var2, Supplier<Vector2f> var3) {
        return (IrisUniformHolder) uniform2f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform2i(IUniformUpdateFrequency var1, String var2, Supplier<Vector2i> var3) {
        return (IrisUniformHolder) uniform2i((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector3f> var3) {
        return (IrisUniformHolder) uniform3f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform3i(IUniformUpdateFrequency var1, String var2, Supplier<Vector3i> var3) {
        return (IrisUniformHolder) uniform3i((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniformTruncated3f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3) {
        return (IrisUniformHolder) uniformTruncated3f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform3d(IUniformUpdateFrequency var1, String var2, Supplier<Vector3d> var3) {
        return (IrisUniformHolder) uniform3d((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform4f(IUniformUpdateFrequency var1, String var2, Supplier<Vector4f> var3) {
        return (IrisUniformHolder) uniform4f((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniform4fArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3) {
        return (IrisUniformHolder) uniform4fArray((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniformMatrix(IUniformUpdateFrequency var1, String var2, Supplier<Matrix4fc> var3) {
        return (IrisUniformHolder) uniformMatrix((UniformUpdateFrequency) (Object) var1, var2, var3);
    }

    @Override
    default IrisUniformHolder uniformMatrixFromArray(IUniformUpdateFrequency var1, String var2, Supplier<float[]> var3) {
        return (IrisUniformHolder) uniformMatrixFromArray((UniformUpdateFrequency) (Object) var1, var2, var3);
    }
}
