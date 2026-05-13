package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import at.redi2go.photonics.core.iris.pipeline.texture.ISamplerHolder;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class SingleFramebuffer extends GlFramebuffer implements InternalIrisFramebuffer {
    private List<FramebufferAttachment> attachments;

    private final FramebufferSize sizeSupplier;
    private final Vector2i currentSize = new Vector2i(-1, -1);

    public SingleFramebuffer(
            List<FramebufferAttachment> attachments,
            FramebufferSize sizeSupplier
    ) {
        this.attachments = ImmutableList.copyOf(attachments);
        this.sizeSupplier = sizeSupplier;

        int[] drawBuffers = new int[attachments.size()];
        for (int i = 0; i < attachments.size(); i++) {
            addColorAttachment(i, ((IGlTexture) attachments.get(i).texture()).handle());
            drawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
        }

        IrisRenderSystem.drawBuffers(getGlId(), drawBuffers);
    }

    public List<FramebufferAttachment> attachments() {
        return attachments;
    }

    @Override
    public Vector2ic viewportSize() {
        return new Vector2i(currentSize);
    }

    @Override
    public void unbind() {
        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        GL11.glViewport(0, 0, width, height);
        Minecraft.getInstance().getMainRenderTarget().iris$bindFramebuffer();
    }

    @Override
    public void flip() {

    }

    @Override
    public void recalculateSizes() {
        var newSize = sizeSupplier.get();
        if (currentSize.equals(newSize)) return;

        currentSize.set(newSize);
        for (FramebufferAttachment attachment : attachments)
            attachment.resize(newSize);
    }

    @Override
    public void addSamplers(ISamplerHolder samplers) {
        for (int i = 0; i < attachments.size(); i++) {
            var attachment = attachments.get(i);
            final int attachmentIndex = i;

            if (attachment.createSampler()) {
                samplers.addDefaultSampler(attachment.name(), () -> attachments.get(attachmentIndex)
                        .texture());
            }
        }
    }

    @Override
    public void close() {
        for (var attachment : attachments)
            attachment.close();
    }
}
