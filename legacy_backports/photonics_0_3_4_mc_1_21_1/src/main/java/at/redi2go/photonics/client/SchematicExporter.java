package at.redi2go.photonics.client;

import at.redi2go.photonics.client.magicavoxel.VoxWriter;
import at.redi2go.photonics.client.rendering.schematics.Schematic;
import at.redi2go.photonics.client.rendering.world.BlockRegistry;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import java.util.zip.ZipOutputStream;
import net.caffeinemc.mods.sodium.client.gl.device.GLRenderDevice;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class SchematicExporter {
   private final BlockState[] blockStates;
   private final Map<Long, Tuple<BlockState, Schematic>> baseCases = new HashMap<>();
   private final ZipWriteContext writeContext;
   private int index;

   public SchematicExporter(File folder) throws FileNotFoundException {
      File zipFile = new File(folder, "schematics.zip");
      if (zipFile.exists()) {
         zipFile.delete();
      }

      this.blockStates = StreamSupport.<Block>stream(BuiltInRegistries.BLOCK.spliterator(), false)
         .flatMap(block -> block.getStateDefinition().getPossibleStates().stream())
         .map(BlockRegistry::cleanUpBlockState)
         .filter(Objects::nonNull)
         .distinct()
         .toArray(BlockState[]::new);
      this.writeContext = new ZipWriteContext(new ZipOutputStream(new FileOutputStream(zipFile)), 16384);
   }

   public boolean exportOne() {
      if (this.index >= this.blockStates.length) {
         try {
            this.writeContext.getZipOutputStream().close();
            return false;
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      } else {
         BlockState blockState = this.blockStates[this.index++];
         BlockState defaultBlockState = blockState.getBlock().defaultBlockState();

         for (Property<?> property : blockState.getProperties()) {
            if (BlockRegistry.DEFAULT_PROPERTIES.contains(property) && !blockState.getValue(property).equals(defaultBlockState.getValue(property))) {
               return true;
            }
         }

         GLRenderDevice.INSTANCE.makeActive();
         exportBlockState(this.writeContext, blockState, this.baseCases);
         GLRenderDevice.INSTANCE.makeInactive();
         return true;
      }
   }

   public float getProgress() {
      return (float)this.index / this.blockStates.length;
   }

   private static void exportBlockState(ZipWriteContext writeContext, BlockState blockState, Map<Long, Tuple<BlockState, Schematic>> baseCases) {
      if (!blockState.isAir()) {
         Schematic schematic = BlockBuilder.buildBlockSchematic(blockState);
         schematic.calcRotationHash();
         int[] targetHash = schematic.getHashVector();
         int ax = Math.abs(targetHash[0]);
         int ay = Math.abs(targetHash[1]);
         int az = Math.abs(targetHash[2]);
         long hash = (long)ax * ax + (long)ay * ay + (long)az * az;
         Tuple<BlockState, Schematic> baseCase = baseCases.get(hash);
         if (baseCase != null) {
            writeContext.write(
               BlockRegistry.encodeBlockState(blockState) + ".alias",
               new String[]{
                  BlockRegistry.encodeBlockState((BlockState)baseCase.getA()),
                  BlockRegistry.encodeTransformation(((Schematic)baseCase.getB()).getHashVector(), targetHash)
               }
            );
         } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            VoxWriter.writeFromSchematic(schematic, byteArrayOutputStream);
            writeContext.write(BlockRegistry.encodeBlockState(blockState) + ".vox", byteArrayOutputStream.toByteArray());
            baseCases.put(hash, new Tuple(blockState, schematic));
         }
      }
   }
}
