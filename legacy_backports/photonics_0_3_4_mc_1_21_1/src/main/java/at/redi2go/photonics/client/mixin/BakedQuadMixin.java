package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.BakedQuadExt;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements BakedQuadExt {
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
