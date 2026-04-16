package at.redi2go.photonics.common.mixins.rendering.world.bakery;

import at.redi2go.photonics.core.rendering.world.bakery.VertexBuilder;
import at.redi2go.photonics.core.rendering.world.block.VoxelColor;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexBuilder.class)
public interface VertexBuilderMixin extends VertexConsumer {
    @Shadow
    VertexBuilder shadow$addVertex(float x, float y, float z);

    @Override
    default @NonNull VertexConsumer addVertex(float x, float y, float z) {
        return (VertexConsumer) shadow$addVertex(x, y, z);
    }

    @Shadow
    VertexBuilder shadow$setTint(int color);

    @Override
    default @NonNull VertexConsumer setColor(int argb) {
        return (VertexConsumer) shadow$setTint(argb);
    }

    @Override
    default @NonNull VertexConsumer setColor(int r, int g, int b, int a) {
        return (VertexConsumer) shadow$setTint(VoxelColor.from(r, g, b, a));
    }

    @Shadow
    VertexBuilder shadow$setUv(float u, float v);

    @Override
    default @NonNull VertexConsumer setUv(float u, float v) {
        return (VertexConsumer) shadow$setUv(u, v);
    }

    @Override
    default @NonNull VertexConsumer setUv1(int i, int j) {
        return this;
    }

    @Override
    default VertexConsumer setUv2(int i, int j) {
        return this;
    }

    @Override
    default VertexConsumer setNormal(float f, float g, float h) {
        return this;
    }

    @Override
   default VertexConsumer setLineWidth(float f) {
        return this;
    }
}
