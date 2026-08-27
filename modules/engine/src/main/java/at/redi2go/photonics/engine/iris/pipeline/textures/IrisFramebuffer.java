package at.redi2go.photonics.engine.iris.pipeline.textures;

import at.redi2go.photonics.game.Disposable;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.engine.RenderingComponent;

import java.util.function.BooleanSupplier;

public interface IrisFramebuffer extends RenderingComponent, Disposable {
    void flip();

    void recalculateSizes();

    interface Builder {
        Builder addAttachment(String name, TextureFormat format, @AttachmentUsage int usage);

        default Builder addAttachment(String name, TextureFormat format, @AttachmentUsage int usage, BooleanSupplier condition) {
            return condition.getAsBoolean() ? addAttachment(name, format, usage) : this;
        }

        IrisFramebuffer build();
    }
}
