package at.redi2go.photonics.common.mixins.meshing;

import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(OuterWrappedRenderType.class)
public interface OuterWrappedRenderTypeAccessor {
    @Accessor
    RenderType getWrapped();
}
