package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.api.gpu.systems.IRenderSystem;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.api.gpu.textures.TextureUsage;
import at.redi2go.photonics.core.iris.pipeline.texture.AttachmentUsage;
import at.redi2go.photonics.core.iris.pipeline.texture.IrisFramebuffer;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

public class IrisFramebufferBuilderImpl implements IrisFramebuffer.Builder {
    private static final int FLIP_PREV_MASK = AttachmentUsage.FLIP | AttachmentUsage.CREATE_PREV_SAMPLER;

    private final FramebufferSize sizeSupplier;
    private final Vector2ic initialSize;

    private final List<FramebufferAttachment> writeAttachments = new ArrayList<>();
    private final List<FramebufferAttachment> readAttachments = new ArrayList<>();

    private int swapAttachmentsCount = 0;

    public IrisFramebufferBuilderImpl(FramebufferSize sizeSupplier) {
        this.sizeSupplier = sizeSupplier;
        initialSize = sizeSupplier.get();
    }

    @Override
    public IrisFramebuffer.Builder addAttachment(String name, ITextureFormat format, @AttachmentUsage int usage) {
        if ((usage & AttachmentUsage.CREATE_PREV_SAMPLER) != 0 && (usage & FLIP_PREV_MASK) != FLIP_PREV_MASK)
            throw new IllegalArgumentException("cannot create prev sampler without flipping");

        final var writeAttachment = IRenderSystem.getDevice()
                .ph$createTexture2D(
                        () -> name + " #1",
                        TextureUsage.RENDER_ATTACHMENT,
                        format,
                        initialSize.x(), initialSize.y(),
                        1
                );

        final IGpuTexture2D readAttachment;

        if ((usage & AttachmentUsage.FLIP) != 0) {
            readAttachment = IRenderSystem.getDevice()
                    .ph$createTexture2D(
                            () -> name + " #2",
                            TextureUsage.RENDER_ATTACHMENT,
                            format,
                            initialSize.x(), initialSize.y(),
                            1
                    );

            swapAttachmentsCount++;
        } else readAttachment = writeAttachment;

        writeAttachments.add(
                new FramebufferAttachment(
                        name,
                        writeAttachment,
                        (usage & AttachmentUsage.CREATE_SAMPLER) != 0,
                        (usage & AttachmentUsage.CREATE_PREV_SAMPLER) != 0
                )
        );

        readAttachments.add(
                new FramebufferAttachment(
                        name,
                        readAttachment,
                        (usage & AttachmentUsage.CREATE_SAMPLER) != 0,
                        (usage & AttachmentUsage.CREATE_PREV_SAMPLER) != 0
                )
        );

        return this;
    }

    @Override
    public IrisFramebuffer build() {
        return swapAttachmentsCount == 0 ? new SingleFramebuffer(
                writeAttachments,
                sizeSupplier
        ) : new FlippableFramebuffer(
                new SingleFramebuffer(writeAttachments, sizeSupplier),
                new SingleFramebuffer(readAttachments, sizeSupplier)
        );
    }
}
