package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.BlockBuilder;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.magicavoxel.VoxReader;
import at.redi2go.photonics.client.mixin.ReloadableResourceManagerAccessor;
import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import at.redi2go.photonics.client.rendering.schematics.AirEntry;
import at.redi2go.photonics.client.rendering.schematics.Schematic;
import at.redi2go.photonics.client.rendering.util.IrisUtil;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryManager;
import at.redi2go.photonics.client.rendering.world.position.PBlockPos;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.io.IOUtils;
import org.joml.Vector3f;

public class BlockRegistry implements Destructable {
   public static final Schematic EMPTY_BLOCK = new Schematic(16, 16, 16);
   public static final Set<Block> VANILLA_RENDERED_BLOCK = Set.of(
      Blocks.WATER,
      Blocks.LAVA,
      Blocks.BARRIER,
      Blocks.END_PORTAL,
      Blocks.END_GATEWAY,
      Blocks.TRIPWIRE,
      Blocks.ZOMBIE_HEAD,
      Blocks.ZOMBIE_WALL_HEAD,
      Blocks.PLAYER_HEAD,
      Blocks.PLAYER_WALL_HEAD,
      Blocks.CREEPER_HEAD,
      Blocks.CREEPER_WALL_HEAD,
      Blocks.DRAGON_HEAD,
      Blocks.DRAGON_WALL_HEAD,
      Blocks.PIGLIN_HEAD,
      Blocks.PIGLIN_WALL_HEAD,
      Blocks.OAK_SIGN,
      Blocks.SPRUCE_SIGN,
      Blocks.BIRCH_SIGN,
      Blocks.ACACIA_SIGN,
      Blocks.CHERRY_SIGN,
      Blocks.JUNGLE_SIGN,
      Blocks.DARK_OAK_SIGN,
      Blocks.MANGROVE_SIGN,
      Blocks.BAMBOO_SIGN,
      Blocks.OAK_WALL_SIGN,
      Blocks.SPRUCE_WALL_SIGN,
      Blocks.BIRCH_WALL_SIGN,
      Blocks.ACACIA_WALL_SIGN,
      Blocks.JUNGLE_WALL_SIGN,
      Blocks.DARK_OAK_WALL_SIGN,
      Blocks.MANGROVE_WALL_SIGN,
      Blocks.BAMBOO_WALL_SIGN,
      Blocks.OAK_HANGING_SIGN,
      Blocks.SPRUCE_HANGING_SIGN,
      Blocks.BIRCH_HANGING_SIGN,
      Blocks.ACACIA_HANGING_SIGN,
      Blocks.CHERRY_WALL_SIGN,
      Blocks.JUNGLE_HANGING_SIGN,
      Blocks.DARK_OAK_HANGING_SIGN,
      Blocks.CRIMSON_HANGING_SIGN,
      Blocks.WARPED_HANGING_SIGN,
      Blocks.MANGROVE_HANGING_SIGN,
      Blocks.BAMBOO_HANGING_SIGN,
      Blocks.OAK_WALL_HANGING_SIGN,
      Blocks.SPRUCE_WALL_HANGING_SIGN,
      Blocks.BIRCH_WALL_HANGING_SIGN,
      Blocks.ACACIA_WALL_HANGING_SIGN,
      Blocks.CHERRY_WALL_HANGING_SIGN,
      Blocks.JUNGLE_WALL_HANGING_SIGN,
      Blocks.DARK_OAK_WALL_HANGING_SIGN,
      Blocks.MANGROVE_WALL_HANGING_SIGN,
      Blocks.CRIMSON_WALL_HANGING_SIGN,
      Blocks.WARPED_WALL_HANGING_SIGN,
      Blocks.BAMBOO_WALL_HANGING_SIGN,
      Blocks.CRIMSON_SIGN,
      Blocks.WARPED_SIGN,
      Blocks.CRIMSON_WALL_SIGN,
      Blocks.WARPED_WALL_SIGN,
      Blocks.WHITE_BANNER,
      Blocks.ORANGE_BANNER,
      Blocks.MAGENTA_BANNER,
      Blocks.LIGHT_BLUE_BANNER,
      Blocks.YELLOW_BANNER,
      Blocks.LIME_BANNER,
      Blocks.PINK_BANNER,
      Blocks.GRAY_BANNER,
      Blocks.LIGHT_GRAY_BANNER,
      Blocks.CYAN_BANNER,
      Blocks.PURPLE_BANNER,
      Blocks.BLUE_BANNER,
      Blocks.BROWN_BANNER,
      Blocks.GREEN_BANNER,
      Blocks.RED_BANNER,
      Blocks.BLACK_BANNER,
      Blocks.WHITE_WALL_BANNER,
      Blocks.ORANGE_WALL_BANNER,
      Blocks.MAGENTA_WALL_BANNER,
      Blocks.LIGHT_BLUE_WALL_BANNER,
      Blocks.YELLOW_WALL_BANNER,
      Blocks.LIME_WALL_BANNER,
      Blocks.PINK_WALL_BANNER,
      Blocks.GRAY_WALL_BANNER,
      Blocks.LIGHT_GRAY_WALL_BANNER,
      Blocks.CYAN_WALL_BANNER,
      Blocks.PURPLE_WALL_BANNER,
      Blocks.BLUE_WALL_BANNER,
      Blocks.BROWN_WALL_BANNER,
      Blocks.GREEN_WALL_BANNER,
      Blocks.RED_WALL_BANNER,
      Blocks.BLACK_WALL_BANNER
   );
   public static final Set<Block> DEFAULT_STATE_BLOCKS = Set.of(Blocks.NOTE_BLOCK);
   public static final Set<Property<?>> DEFAULT_PROPERTIES = Set.of(
      BlockStateProperties.PERSISTENT, BlockStateProperties.DISTANCE, BlockStateProperties.WATERLOGGED, BlockStateProperties.OCCUPIED
   );
   private final MemoryManager memoryManager;
   private final Map<BlockState, PBlock> blockSchematicCache = new ConcurrentHashMap<>();
   private final PreparableReloadListener resourceReloadListener = (barrier, manager, profiler1, profiler2, bgExecutor, gameExecutor) -> barrier.wait(
         null
      )
      .thenRunAsync(this::reloadBlockModels, gameExecutor);

   public BlockRegistry(MemoryManager memoryManager) {
      ((ReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(this.resourceReloadListener);
      this.memoryManager = memoryManager;
      memoryManager.allocate(PBlock.BYTE_SIZE);
      PBlock.numAllocated = 0;
   }

   public void freeUnused() {
      for (Entry<BlockState, PBlock> e : this.blockSchematicCache.entrySet()) {
         BlockState blockState = e.getKey();
         PBlock block = e.getValue();
         if (!block.isUsed() && block.isAllocated()) {
            synchronized (block) {
               block.free(this.memoryManager);
               this.blockSchematicCache.remove(blockState);
            }
         }
      }
   }

   public void ensureAllocated(PBlock block) {
      synchronized (block) {
         if (block.isAllocated()) {
            if (block.needsUpdate()) {
               block.update(this.memoryManager);
            }
         } else {
            block.changeTimesUsed(1);
            if (PBlock.numAllocated >= 8191) {
               this.freeUnused();
            }

            block.changeTimesUsed(-1);
            block.allocate(this.memoryManager);
            block.update(this.memoryManager);
         }
      }
   }

   public boolean upload() {
      for (PBlock block : this.blockSchematicCache.values()) {
         if (block.needsUpdate() && block.isAllocated()) {
            block.update(this.memoryManager);
         }
      }

      return this.memoryManager.upload();
   }

   public PBlock getBlock(PBlockPos blockPosition) {
      Level level = Minecraft.getInstance().level;
      if (level == null) {
         return null;
      }

      BlockState blockState;
      try {
         blockState = level.getBlockState(new BlockPos(blockPosition.x, blockPosition.y, blockPosition.z));
      } catch (Exception ignore) {
         blockState = Blocks.AIR.defaultBlockState();
      }

      return this.getBlock(blockState);
   }

   public PBlock getBlock(BlockState blockState) {
      blockState = cleanUpBlockState(blockState);
      if (blockState == null) {
         return null;
      }

      PBlock block = this.blockSchematicCache.get(blockState);
      if (block != null) {
         return block;
      }

      block = new PBlock(IrisUtil.getBlockId(blockState), () -> EMPTY_BLOCK);
      Raytracer.INSTANCE.getWorldRegistry().getLightRegistry().registerBlockState(blockState, block);
      this.blockSchematicCache.put(blockState, block);
      Schematic schematic = loadSchematicFromDisk(blockState, false);
      if (schematic == null) {
         BlockBuilder.streamBlockBuild(blockState, block);
      } else {
         schematic.initialize();
         PBlock finalBlock = block;
         schematic.optimizeThreaded().thenRun(() -> Raytracer.INSTANCE.queueUrgentBuildJob(() -> finalBlock.setCompiledSchematicSupplier(() -> schematic)));
      }

      return block;
   }

   public void reloadBlockModels() {
      Raytracer.INSTANCE
         .queueUrgentBuildJob(
            () -> {
               for (Entry<BlockState, PBlock> entry : this.blockSchematicCache.entrySet()) {
                  Schematic schematic = loadSchematicFromDisk(entry.getKey(), false);
                  if (schematic == null) {
                     BlockBuilder.streamBlockBuild(entry.getKey(), entry.getValue());
                  } else {
                     schematic.initialize();
                     schematic.optimizeThreaded()
                        .thenRun(() -> Raytracer.INSTANCE.queueUrgentBuildJob(() -> entry.getValue().setCompiledSchematicSupplier(() -> schematic)));
                  }
               }
            }
         );
   }

   @Override
   public void free() {
      this.blockSchematicCache.clear();
      PBlock.numAllocated = 0;
      ((ReloadableResourceManagerAccessor)Minecraft.getInstance().getResourceManager()).getListeners().remove(this.resourceReloadListener);
      this.memoryManager.free();
   }

   public Map<BlockState, PBlock> getBlockSchematicCache() {
      return this.blockSchematicCache;
   }

   private static Schematic loadSchematicFromDisk(BlockState blockState, boolean aliased) {
      ResourceProvider resourceFactory = Minecraft.getInstance().getResourceManager();
      if (!aliased) {
         try {
            Optional<Resource> resource = resourceFactory.getResource(ResourceLocation.withDefaultNamespace("schematics/" + encodeBlockState(blockState) + ".alias"));
            if (resource.isPresent()) {
               List<String> lines = IOUtils.readLines(resource.get().open(), Charset.defaultCharset());
               if (lines.size() >= 2) {
                  BlockState baseBlockState = decodeBlockState(lines.get(0));
                  Consumer<Vector3f> transformation = decodeTransformation(lines.get(1));
                  Schematic schematic = loadSchematicFromDisk(baseBlockState, true);
                  if (baseBlockState != null && schematic != null) {
                     schematic = schematic.transform(transformation);
                     if (schematic != null) {
                        return schematic;
                     }
                  }
               }
            }
         } catch (IOException var9) {
         }
      }

      try {
         Optional<Resource> resource = resourceFactory.getResource(ResourceLocation.withDefaultNamespace("schematics/" + encodeBlockState(blockState) + ".vox"));
         if (resource.isPresent()) {
            Schematic schematic = VoxReader.readToSchematic(resource.get().open());
            if (schematic.getWidth() == 16 && schematic.getHeight() == 16 && schematic.getDepth() == 16) {
               return schematic;
            }
         }
      } catch (IOException var8) {
      }

      return null;
   }

   public static String encodeTransformation(int[] baseHash, int[] targetHash) {
      StringBuilder builder = new StringBuilder();
      boolean[] usedAxis = new boolean[3];
      int matches = 0;

      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            if (!usedAxis[j] && Math.abs(baseHash[j]) == Math.abs(targetHash[i])) {
               int sign = baseHash[j] != targetHash[i] ? -1 : 1;
               builder.append(sign * (j + 1)).append(' ');
               usedAxis[j] = true;
               matches++;
               break;
            }
         }
      }

      if (matches < 3) {
         System.err.println("Incorrect number of matches");
         return "1 2 3";
      } else {
         return builder.toString();
      }
   }

   public static Consumer<Vector3f> decodeTransformation(String encodedTransformation) {
      int[] transformation = Arrays.stream(encodedTransformation.split(" ")).mapToInt(Integer::parseInt).toArray();
      int[] indices = new int[3];
      int[] signs = new int[3];
      int[] offsets = new int[3];

      for (int i = 0; i < 3; i++) {
         indices[i] = Math.abs(transformation[i]) - 1;
         signs[i] = transformation[i] < 0 ? -1 : 1;
         offsets[i] = transformation[i] < 0 ? 15 : 0;
      }

      return v -> v.set(offsets[0] + signs[0] * v.get(indices[0]), offsets[1] + signs[1] * v.get(indices[1]), offsets[2] + signs[2] * v.get(indices[2]));
   }

   public static String encodeBlockState(BlockState blockState) {
      StringBuilder builder = new StringBuilder();
      builder.append(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath());
      blockState.getValues().forEach((p, v) -> {
         if (!DEFAULT_PROPERTIES.contains(p)) {
            builder.append('-').append(p.getName().toLowerCase(Locale.ENGLISH)).append('_').append(v.toString().toLowerCase(Locale.ENGLISH));
         }
      });
      return builder.toString();
   }

   public static BlockState decodeBlockState(String encodedBlockState) {
      String blockName = encodedBlockState.split("-")[0];
      Optional<ImmutableList<BlockState>> states = BuiltInRegistries.BLOCK
         .getOptional(ResourceLocation.withDefaultNamespace(blockName))
         .map(block -> block.getStateDefinition().getPossibleStates());
      if (states.isEmpty()) {
         return null;
      }

      UnmodifiableIterator var3 = states.get().iterator();

      while (var3.hasNext()) {
         BlockState blockState = (BlockState)var3.next();
         if (encodeBlockState(blockState).equals(encodedBlockState)) {
            return blockState;
         }
      }

      return null;
   }

   private static boolean isVanillaRendered(Block block) {
      return Raytracer.getProperties().map(e -> e.voxelizeLava().orElse(false)).orElse(false) ? false : VANILLA_RENDERED_BLOCK.contains(block);
   }

   public static BlockState cleanUpBlockState(BlockState blockState) {
      if (!blockState.isAir() && !isVanillaRendered(blockState.getBlock())) {
         if (DEFAULT_STATE_BLOCKS.contains(blockState.getBlock())) {
            blockState = blockState.getBlock().defaultBlockState();
         }

         return blockState;
      } else {
         return null;
      }
   }

   static {
      Arrays.fill(EMPTY_BLOCK.getData(), AirEntry.toAirEntry(0, 0, 0, 16, 16, 16));
      EMPTY_BLOCK.setState(2);
   }
}
