package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.common.mixins.meshing.RenderSetupAccessor;
import at.redi2go.photonics.common.mixins.meshing.RenderTypeAccessor;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.NonNull;

public class BlockBuilderBufferSource extends MultiBufferSource.BufferSource {
    private BlockBuilder blockBuilder;

    public BlockBuilderBufferSource() {
        super(null, null);
    }

    public void setBlockBuilder(BlockBuilder blockBuilder) {
        this.blockBuilder = blockBuilder;
    }

    @Override
    public @NonNull VertexConsumer getBuffer(@NonNull RenderType renderType) {
        RenderSetup renderSetup = ((RenderTypeAccessor) renderType).getState();
        var textures = ((RenderSetupAccessor) (Object) renderSetup).getTextureSetup();
        var sampler0 = textures.get("Sampler0");

        if (sampler0 == null) return EmptyVertexConsumer.INSTANCE;

        blockBuilder.useAtlas((Id) (Object) sampler0.getLocation());

        return (VertexConsumer) blockBuilder;
    }

    @Override
    public void endLastBatch() {

    }

    @Override
    public void endBatch() {

    }

    @Override
    public void endBatch(@NonNull RenderType renderType) {

    }
}
