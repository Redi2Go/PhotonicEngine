package at.redi2go.photonics.common.meshing;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.NonNull;

public class EmptyOutlineBufferSource extends OutlineBufferSource {
    public static final EmptyOutlineBufferSource INSTANCE = new EmptyOutlineBufferSource();

    private EmptyOutlineBufferSource() {
    }

    @Override
    public VertexConsumer getBuffer(@NonNull RenderType renderType) {
        return EmptyVertexConsumer.INSTANCE;
    }

    @Override
    public void setColor(int i) {

    }

    @Override
    public void endOutlineBatch() {

    }
}
