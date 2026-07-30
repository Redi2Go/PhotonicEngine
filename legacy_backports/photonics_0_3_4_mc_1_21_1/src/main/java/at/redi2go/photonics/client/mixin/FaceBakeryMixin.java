package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.BakedQuadExt;
import at.redi2go.photonics.client.BlockElementFaceExt;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FaceBakery.class)
public class FaceBakeryMixin {
   @Inject(method = "bakeQuad", at = @At("RETURN"))
   public void bakeQuad(
      Vector3f posFrom,
      Vector3f posTo,
      BlockElementFace face,
      TextureAtlasSprite sprite,
      Direction direction,
      ModelState modelState,
      BlockElementRotation rotation,
      boolean shade,
      CallbackInfoReturnable<BakedQuad> cir
   ) {
      BakedQuad bakedQuad = cir.getReturnValue();
      boolean shouldBeVoxelized = ((BlockElementFaceExt)(Object)face).photonics$shouldBeVoxelized();
      ((BakedQuadExt)(Object)bakedQuad).photonics$setShouldBeVoxelized(shouldBeVoxelized);
   }
}
