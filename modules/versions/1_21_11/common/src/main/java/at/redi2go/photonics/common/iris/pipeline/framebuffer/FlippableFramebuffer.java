package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import org.joml.Vector2ic;

public class FlippableFramebuffer implements InternalIrisFramebuffer {
    private SingleFramebuffer write;
    private SingleFramebuffer read;

    public FlippableFramebuffer(SingleFramebuffer write, SingleFramebuffer read) {
        if (write.attachments().size() != read.attachments().size())
            throw new IllegalArgumentException("attachments (" + write.attachments().size() + ") and swap (" + read.attachments().size() + ") must have the same size");

        this.write = write;
        this.read = read;
    }

    @Override
    public void flip() {
        var tmp = write;
        write = read;
        read = tmp;
    }

    @Override
    public Vector2ic viewportSize() {
        return write.viewportSize();
    }

    @Override
    public void bind() {
        write.bind();
    }

    @Override
    public void unbind() {
        write.unbind();
    }

    @Override
    public void recalculateSizes() {
        write.recalculateSizes();
        read.recalculateSizes();
    }

    @Override
    public void registerCustomTextures(ISamplerHolder samplers) {
        var writeAttachments = write.attachments();

        for (int i = 0; i < writeAttachments.size(); i++) {
            var attachment = writeAttachments.get(i);
            final int attachmentIndex = i;

            if (attachment.createSampler()) {
                samplers.addDefaultSampler(attachment.name(), () -> write.attachments().get(attachmentIndex)
                        .texture());
            }

            if (attachment.createPrevSampler()) {
                samplers.addDefaultSampler("prev_" + attachment.name(), () -> read.attachments().get(attachmentIndex)
                        .texture());
            }
        }

    }

    @Override
    public void close() {
        write.close();
        read.close();
    }
}
