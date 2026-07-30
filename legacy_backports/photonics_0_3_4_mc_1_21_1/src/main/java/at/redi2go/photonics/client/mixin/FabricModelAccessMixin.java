package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.BakedQuadExt;
import at.redi2go.photonics.client.BlockBuilder;
import java.util.List;
import net.caffeinemc.mods.sodium.fabric.model.FabricModelAccess;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FabricModelAccess.class, remap = false)
public class FabricModelAccessMixin {
   @Inject(method = "getQuads", at = @At("RETURN"), cancellable = true)
   public void getQuads(BlockAndTintGetter level, BlockPos pos, BakedModel model, BlockState state, Direction face, RandomSource random, RenderType renderType, SodiumModelData modelData, CallbackInfoReturnable<List<BakedQuad>> cir) {
      if (BlockBuilder.BLOCK_BUILDING_ACTIVE) {
         List<BakedQuad> original = cir.getReturnValue();
         if (original != null) {
            cir.setReturnValue(original.stream().filter(bakedQuad -> ((BakedQuadExt)bakedQuad).photonics$shouldBeVoxelized()).toList());
         }
      }
   }
}
