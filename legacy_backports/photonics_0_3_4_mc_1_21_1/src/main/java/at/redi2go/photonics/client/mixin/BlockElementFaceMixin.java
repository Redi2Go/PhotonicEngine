package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.BlockElementFaceExt;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockElementFace.class)
public class BlockElementFaceMixin implements BlockElementFaceExt {
   @Unique
   private boolean shouldBeVoxelized = true;

   @Override
   public boolean photonics$shouldBeVoxelized() {
      return this.shouldBeVoxelized;
   }

   @Override
   public void photonics$setShouldBeVoxelized(boolean shouldBeVoxelized) {
      this.shouldBeVoxelized = shouldBeVoxelized;
   }
}
