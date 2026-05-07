package at.redi2go.photonics.core.iris.pipeline.texture;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import org.joml.Vector2ic;

public interface IrisFramebuffer extends Disposable {
    void flip();

    void recalculateSizes();

    void addSamplers(ISamplerHolder samplers);

    interface Builder {
        Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage);

        IrisFramebuffer build();
    }
}
