package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.config.PhotonicsConfig;
import at.redi2go.photonics.client.rendering.world.PBlock;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockOcclusionCache.class, remap = false)
public class BlockOcclusionCacheMixin {
   @Shadow
   @Final
   private MutableBlockPos cachedPositionObject;

   @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
   public void shouldDrawSide(BlockState selfBlockState, BlockGetter view, BlockPos selfPos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
      MutableBlockPos neighborPos = this.cachedPositionObject;
      neighborPos.setWithOffset(selfPos, facing);
      BlockState neighborBlockState = view.getBlockState(neighborPos);
      if (PhotonicsConfig.isVoxelizedBlocksEnabled() && PhotonicsConfig.isVoxelized(neighborBlockState.getBlock())) {
         PBlock block = Raytracer.INSTANCE != null ? Raytracer.INSTANCE.getBlockRegistry().getBlock(neighborBlockState) : null;
         if (block != null && !block.canOcclude) {
            cir.setReturnValue(true);
            cir.cancel();
         }
      }
   }
}
