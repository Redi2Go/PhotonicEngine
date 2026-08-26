package at.redi2go.photonics.common.iris.pipeline.uniforms;

import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisDynamicUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IrisUniformHolder;
import at.redi2go.photonics.engine.iris.pipeline.uniforms.IValueUpdateNotifier;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@Mixin(DynamicUniformHolder.class)
public interface IrisDynamicUniformHolderImpl extends DynamicUniformHolder, IrisDynamicUniformHolder {
    @Override
    default IrisUniformHolder uniform1f(String var1, IntSupplier var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform1f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform1f(String var1, DoubleSupplier var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform1f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform1i(String var1, IntSupplier var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform1i(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform2f(String var1, Supplier<Vector2f> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform2f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform2i(String var1, Supplier<Vector2i> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform2i(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform3f(String var1, Supplier<Vector3f> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform3f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform4f(String var1, Supplier<Vector4f> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform4f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform4fArray(String var1, Supplier<float[]> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform4fArray(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniform4i(String var1, Supplier<Vector4i> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniform4i(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniformMatrix(String var1, Supplier<Matrix4fc> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniformMatrix(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IrisUniformHolder uniformMatrix3(String var1, Supplier<Matrix3fc> var2, IValueUpdateNotifier var3) {
        return (IrisUniformHolder) uniformMatrix3(var1, var2, (ValueUpdateNotifier) var3);
    }
}
