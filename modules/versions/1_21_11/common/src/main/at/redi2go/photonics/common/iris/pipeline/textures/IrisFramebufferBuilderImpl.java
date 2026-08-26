package at.redi2go.photonics.common.iris.pipeline.textures;

import at.redi2go.photonics.game.blaze3d.systems.IGpuDevice;
import at.redi2go.photonics.game.blaze3d.systems.IRenderSystem;
import at.redi2go.photonics.game.blaze3d.textures.IGpuTexture;
import at.redi2go.photonics.game.blaze3d.textures.TextureFormat;
import at.redi2go.photonics.game.blaze3d.textures.TextureUsage;
import at.redi2go.photonics.engine.iris.pipeline.textures.AttachmentUsage;
import at.redi2go.photonics.engine.iris.pipeline.textures.IrisFramebuffer;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class IrisFramebufferBuilderImpl implements IrisFramebuffer.Builder {
    private final FramebufferSize sizeSupplier;
    private final Vector2ic initialSize;
    private final Consumer<IrisFramebuffer> registration;

    private final List<FramebufferAttachment> writeAttachments = new ArrayList<>();
    private final List<FramebufferAttachment> readAttachments = new ArrayList<>();

    private boolean isFlippable = false;

    public IrisFramebufferBuilderImpl(FramebufferSize sizeSupplier, Consumer<IrisFramebuffer> registration) {
        this.sizeSupplier = sizeSupplier;
        initialSize = sizeSupplier.get();

        this.registration = registration;
    }

    @Override
    public IrisFramebuffer.Builder addAttachment(String name, TextureFormat format, @AttachmentUsage int usage) {
        isFlippable |= (usage & AttachmentUsage.FLIP) == 0;

        final var writeAttachment = IGpuDevice.createTexture(
                () -> name + " main",
                TextureUsage.RENDER_ATTACHMENT | TextureUsage.RESIZEABLE,
                format,
                initialSize,
                1
        );

        final var readAttachment = (usage & AttachmentUsage.FLIP) == 0 ? writeAttachment : IGpuDevice.createTexture(
                () -> name + " alt",
                TextureUsage.RENDER_ATTACHMENT | TextureUsage.RESIZEABLE,
                format,
                initialSize,
                1
        );

        writeAttachments.add(
                new FramebufferAttachment(
                        name,
                        writeAttachment,
                        (usage & AttachmentUsage.CREATE_SAMPLER) != 0,
                        (usage & AttachmentUsage.FLIP) != 0
                )
        );

        readAttachments.add(
                new FramebufferAttachment(
                        name,
                        readAttachment,
                        (usage & AttachmentUsage.CREATE_SAMPLER) != 0,
                        (usage & AttachmentUsage.FLIP) != 0
                )
        );



        return this;
    }

    @Override
    public IrisFramebuffer build() {
        if (writeAttachments.isEmpty()) return EmptyFramebuffer.INSTANCE;

        var result = (isFlippable ? new SingleFramebuffer(
                writeAttachments,
                sizeSupplier
        ) : new FlippableFramebuffer(
                new SingleFramebuffer(writeAttachments, sizeSupplier),
                new SingleFramebuffer(readAttachments, sizeSupplier)
        ));

        registration.accept(result);

        return result;
    }
}
