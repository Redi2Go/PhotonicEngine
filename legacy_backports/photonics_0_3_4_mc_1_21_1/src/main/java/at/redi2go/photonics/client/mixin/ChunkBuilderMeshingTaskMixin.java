package at.redi2go.photonics.client.mixin;

import at.redi2go.photonics.client.BlockRendererExt;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.config.PhotonicsConfig;
import at.redi2go.photonics.client.rendering.world.LightRegistry;
import at.redi2go.photonics.client.rendering.world.PBlock;
import at.redi2go.photonics.client.rendering.world.VoxelFallbackDiagnostics;
import at.redi2go.photonics.client.rendering.world.position.PChunkPos;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.locks.Lock;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkBuilderMeshingTask.class)
public class ChunkBuilderMeshingTaskMixin {
   private static boolean photonics$isVoxelReplacementReady(BlockState state) {
      if (Raytracer.isDisabled() || !PhotonicsConfig.isVoxelizedBlocksEnabled() || !PhotonicsConfig.isVoxelized(state.getBlock())) {
         return false;
      }

      PBlock block = Raytracer.INSTANCE.getBlockRegistry().getBlock(state);
      return block != null && block.isVoxelRenderReady();
   }

   @Inject(
      method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
      at = @At("TAIL"),
      remap = false
   )
   public void execute(
      ChunkBuildContext buildContext,
      CancellationToken cancellationToken,
      CallbackInfoReturnable<ChunkBuildOutput> cir,
      @Local(ordinal = 0) int minX,
      @Local(ordinal = 1) int minY,
      @Local(ordinal = 2) int minZ,
      @Local(ordinal = 3) int maxX,
      @Local(ordinal = 4) int maxY,
      @Local(ordinal = 5) int maxZ
   ) {
      if (!Raytracer.isDisabled()) {
         BlockPos lowerCorner = new BlockPos(minX, minY, minZ);
         BlockPos upperCorner = new BlockPos(maxX, maxY, maxZ);
         Raytracer.INSTANCE
            .getWorldRegistry()
            .queueBuildJob(
               () -> Raytracer.INSTANCE.getWorldRegistry().loadChunk(new PChunkPos(lowerCorner.getX() / 16, lowerCorner.getY() / 16, lowerCorner.getZ() / 16))
            );
         Raytracer.INSTANCE.getWorldRegistry().wakeUpWorldBuilder();
         LightRegistry lightRegistry = Raytracer.INSTANCE.getWorldRegistry().getLightRegistry();
         Lock lock = lightRegistry.readLock();

         try {
            lock.lock();

            for (BlockPos blockPos : BlockPos.betweenClosed(lowerCorner, upperCorner)) {
               lightRegistry.onBlockLoad(new Vector3f(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            }
         } finally {
            lock.unlock();
         }
      }
   }

   @Redirect(
      method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z")
   )
   public boolean hasBlockEntity(BlockState instance) {
      return photonics$isVoxelReplacementReady(instance) ? false : instance.hasBlockEntity();
   }

   @Redirect(
      method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;")
   )
   public RenderShape getRenderShape(
      BlockState instance, @Local BlockRenderCache cache, @Local(ordinal = 0) MutableBlockPos blockPos, @Local(ordinal = 1) MutableBlockPos modelOffset
   ) {
      if (photonics$isVoxelReplacementReady(instance)) {
         PBlock block = Raytracer.INSTANCE.getBlockRegistry().getBlock(instance);
         BlockState blockState = block != null && block.canOcclude ? Blocks.STONE.defaultBlockState() : Blocks.OAK_LEAVES.defaultBlockState();
         BakedModel model = cache.getBlockModels().getModelManager().getMissingModel();
         ((BlockRendererExt)cache.getBlockRenderer()).photonics$setRenderingVoxelBlock(true);
         cache.getBlockRenderer().renderModel(model, blockState, blockPos, modelOffset);
         ((BlockRendererExt)cache.getBlockRenderer()).photonics$setRenderingVoxelBlock(false);
         return RenderShape.INVISIBLE;
      } else {
         if (!Raytracer.isDisabled() && PhotonicsConfig.isVoxelizedBlocksEnabled() && PhotonicsConfig.isVoxelized(instance.getBlock())) {
            int fallbackCount = VoxelFallbackDiagnostics.recordFallback();
            if (fallbackCount == 1) {
               at.redi2go.photonics.client.Photonics.warn(
                  "Voxel replacement is not ready; preserving Sodium models. Additional fallbacks are counted without per-block logging."
               );
            }
         }

         return instance.getRenderShape();
      }
   }
}
