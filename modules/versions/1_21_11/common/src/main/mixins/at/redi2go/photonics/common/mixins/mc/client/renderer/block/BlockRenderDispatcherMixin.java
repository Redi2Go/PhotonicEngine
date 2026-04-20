package at.redi2go.photonics.common.mixins.mc.client.renderer.block;

import at.redi2go.photonics.common.BlockRenderDispatcherExt;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderDispatcherMixin implements BlockRenderDispatcherExt {
    @Shadow
    @Final
    private ModelBlockRenderer modelRenderer;

    @Override
    public ModelBlockRenderer photonics$modelBlockRenderer() {
        return modelRenderer;
    }
}
