package at.redi2go.photonics.client;

import at.redi2go.photonics.client.rendering.opengl.objects.GlTarget;
import at.redi2go.photonics.client.rendering.schematics.Schematic;
import at.redi2go.photonics.client.rendering.world.PBlock;
import at.redi2go.photonics.client.rendering.world.buffer.GlMemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryOwner;
import at.redi2go.photonics.client.rendering.world.buffer.SimpleMemoryOwner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL42;

public class BlockBuilder {
   private static final RenderBuffers MULTI_BUFFER_SOURCE = new RenderBuffers(16384);
   public static boolean BLOCK_BUILDING_ACTIVE = false;
   public static final GlMemoryManager SCHEMATIC_MEMORY_MANAGER = new GlMemoryManager(GlTarget.SSBO, "world_array_block", 16384, false);
   private static final MemoryOwner SCHEMATIC_MEMORY = new SimpleMemoryOwner(SCHEMATIC_MEMORY_MANAGER, SCHEMATIC_MEMORY_MANAGER.getCapacity());
   public static int RENDER_INDEX = 0;

   public static void streamBlockBuild(BlockState blockState, PBlock block) {
      Raytracer.INSTANCE.queueOpenGLJob(() -> {
         GLRenderDevice.INSTANCE.makeActive();
         Schematic schematic = buildBlockSchematic(blockState);
         GLRenderDevice.INSTANCE.makeInactive();
         schematic.initialize();
         Raytracer.INSTANCE.queueUrgentBuildJob(() -> block.setCompiledSchematicSupplier(() -> schematic));
         schematic.optimizeThreaded().thenRun(() -> Raytracer.INSTANCE.queueUrgentBuildJob(() -> block.setCompiledSchematicSupplier(() -> schematic)));
      });
   }

   public static Schematic buildBlockSchematic(BlockState blockState) {
      BLOCK_BUILDING_ACTIVE = true;
      Minecraft minecraft = Minecraft.getInstance();
      Minecraft.getInstance()
         .getBlockRenderer()
         .renderSingleBlock(blockState, new PoseStack(), MULTI_BUFFER_SOURCE.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
      if (blockState.hasBlockEntity()) {
         EntityBlock entityBlock = (EntityBlock)blockState.getBlock();
         BlockEntity entity = entityBlock.newBlockEntity(new BlockPos(0, 0, 0), blockState);
         if (entity != null) {
            entity.setLevel(minecraft.level);
            BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (renderer != null) {
               renderer.render(entity, 0.0F, new PoseStack(), MULTI_BUFFER_SOURCE.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY);
            }
         }
      }

      SCHEMATIC_MEMORY_MANAGER.queueUpload(SCHEMATIC_MEMORY);
      SCHEMATIC_MEMORY_MANAGER.upload();
      MULTI_BUFFER_SOURCE.bufferSource().endBatch();
      GL11.glFinish();
      GL42.glMemoryBarrier(512);
      GL42.glMemoryBarrier(8192);
      int[] schematicData = new int[SCHEMATIC_MEMORY.getSize() >> 2];
      SCHEMATIC_MEMORY_MANAGER.download(byteBuffer -> byteBuffer.asIntBuffer().get(schematicData));
      BLOCK_BUILDING_ACTIVE = false;

      for (int i = 0; i < schematicData.length; i++) {
         int color = schematicData[i];
         int alpha = color & 127;
         schematicData[i] = color >> 7 | alpha << 24;
      }

      return new Schematic(schematicData, 16, 16, 16);
   }
}
