package at.redi2go.photonics.core.iris.pipeline.texture;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import org.joml.Vector2ic;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public interface IrisFramebuffer extends RenderingComponent, Disposable {
    void flip();

    void recalculateSizes();

    interface Builder {
        Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage);

        default Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage, BooleanSupplier condition) {
            return condition.getAsBoolean() ? addAttachment(name, format, usage) : this;
        }

        IrisFramebuffer build(Function<IrisFramebuffer, IrisFramebuffer> registration);

        default IrisFramebuffer build() {
            return build(Function.identity());
        }
    }
}
