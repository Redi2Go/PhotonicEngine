package at.redi2go.photonics.core.iris.pipeline.texture;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import org.joml.Vector2ic;

public interface IrisFramebuffer extends RenderingComponent, Disposable {
    void flip();

    void recalculateSizes();

    interface Builder {
        Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage);

        IrisFramebuffer build();
    }
}
