package at.redi2go.photonics.common.mixins.iris.pipeline.passes.composite;

import at.redi2go.photonics.common.iris.pipeline.CompositeRendererPassExt;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.InternalIrisFramebuffer;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(targets = "net.irisshaders.iris.pipeline.CompositeRenderer$Pass")
public abstract class CompositePassMixin implements CompositeRendererPassExt {
    @Unique
    private static final ViewportData PH_VIEWPORT = new ViewportData(1, 0, 0);

    @Shadow int viewWidth;
    @Shadow int viewHeight;

    @Shadow ViewportData viewportScale;

    @Unique private int index = 0;

    @Shadow private String name;
    @Unique private String debugName = null;
    @Unique private ImmutableList<Runnable> actions = ImmutableList.of();
    @Unique private InternalIrisFramebuffer phFramebuffer = null;

    @WrapOperation(
            method = "setupState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/framebuffer/GlFramebuffer;bind()V"
            )
    )
    public void bindFramebuffer(GlFramebuffer instance, Operation<Void> original) {
        if (phFramebuffer != null)
            phFramebuffer.bind();
        else
            original.call(instance);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getDebugName() {
        return Optional.ofNullable(debugName)
                .orElse(name);
    }

    @Override
    public void setDebugName(String debugName) {
        this.debugName = debugName;
    }

    @Override
    public ImmutableList<Runnable> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<Runnable> actions) {
        this.actions = ImmutableList.copyOf(actions);
    }

    @Override
    public Optional<IrisFramebuffer> getFramebuffer() {
        return Optional.ofNullable(phFramebuffer);
    }

    @Override
    public void setFramebuffer(@Nullable IrisFramebuffer framebuffer) {
        this.phFramebuffer = (InternalIrisFramebuffer) framebuffer;
    }

    @Override
    public void updateSize() {
        if (phFramebuffer == null) return;

        var size = phFramebuffer.viewportSize();

        this.viewWidth = size.x();
        this.viewHeight = size.y();

        this.viewportScale = PH_VIEWPORT;
    }
}
