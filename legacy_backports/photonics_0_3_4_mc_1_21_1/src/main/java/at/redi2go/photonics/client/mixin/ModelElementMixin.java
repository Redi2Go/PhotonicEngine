package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.Raytracer;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockElement.class)
public class ModelElementMixin {
   @Mutable
   @Final
   @Shadow
   private Map<Direction, BlockElementFace> faces;

   @Inject(
      method = "<init>(Lorg/joml/Vector3f;Lorg/joml/Vector3f;Ljava/util/Map;Lnet/minecraft/client/renderer/block/model/BlockElementRotation;Z)V",
      at = @At("TAIL")
   )
   private void init(Vector3f from, Vector3f to, Map map, BlockElementRotation blockElementRotation, boolean bl, CallbackInfo ci) {
      Raytracer.filterDoubleFaces(this.faces, new Vector3f(from), new Vector3f(to));
   }
}
