package at.redi2go.photonics.impl.mixins.shaders.iris.uniform;

import at.redi2go.photonics.api.shaders.uniform.IDynamicUniformHolder;
import at.redi2go.photonics.api.shaders.uniform.IUniformHolder;
import at.redi2go.photonics.api.shaders.uniform.IValueUpdateNotifier;
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
public interface DynamicUniformHolderMixin extends DynamicUniformHolder, IDynamicUniformHolder {
    @Override
    default IUniformHolder uniform1f(String var1, IntSupplier var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform1f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform1f(String var1, DoubleSupplier var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform1f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform1i(String var1, IntSupplier var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform1i(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform2f(String var1, Supplier<Vector2f> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform2f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform2i(String var1, Supplier<Vector2i> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform2i(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform3f(String var1, Supplier<Vector3f> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform3f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform4f(String var1, Supplier<Vector4f> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform4f(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform4fArray(String var1, Supplier<float[]> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform4fArray(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniform4i(String var1, Supplier<Vector4i> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniform4i(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniformMatrix(String var1, Supplier<Matrix4fc> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniformMatrix(var1, var2, (ValueUpdateNotifier) var3);
    }

    @Override
    default IUniformHolder uniformMatrix3(String var1, Supplier<Matrix3fc> var2, IValueUpdateNotifier var3) {
        return (IUniformHolder) uniformMatrix3(var1, var2, (ValueUpdateNotifier) var3);
    }
}
