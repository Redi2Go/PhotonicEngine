package at.redi2go.photonics.common.mixins.iris.pipeline.passes.composite;

import at.redi2go.photonics.common.iris.pipeline.CompositeRendererPassExt;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.InternalIrisFramebuffer;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(targets = "net.irisshaders.iris.pipeline.CompositeRenderer$ComputeOnlyPass")
public abstract class CompositeComputePassMixin implements CompositeRendererPassExt {
    @Override
    public Optional<IrisFramebuffer> getFramebuffer() {
        return Optional.empty();
    }

    @Override
    public void setFramebuffer(@Nullable IrisFramebuffer framebuffer) {
        // Nothing
    }

    @Override
    public void updateSize() {
        // Nothing
    }
}
