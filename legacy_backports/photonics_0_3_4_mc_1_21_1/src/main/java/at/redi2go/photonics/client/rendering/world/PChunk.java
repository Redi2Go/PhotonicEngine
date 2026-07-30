package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.rendering.schematics.Schematic;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryOwner;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryRegion;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.concurrent.ExecutionException;

public class PChunk implements MemoryOwner {
   public static final int CHUNK_SIZE = 16;
   private final Schematic schematic;
   private MemoryRegion chunkMemory;
   private boolean dirty = true;
   private final Object2IntMap<PBlock> blocks;
   public static int loaded = 0;

   public PChunk() {
      this.schematic = new Schematic(16, 16, 16);
      this.blocks = new Object2IntOpenHashMap();
   }

   public void freeBlocks() {
      ObjectIterator var1 = this.blocks.object2IntEntrySet().iterator();

      while (var1.hasNext()) {
         Entry<PBlock> e = (Entry<PBlock>)var1.next();
         ((PBlock)e.getKey()).changeTimesUsed(-e.getIntValue());
      }

      this.blocks.clear();
   }

   public void set(int x, int y, int z, PBlock block, int skyBrightness) {
      int value;
      if (block != null) {
         value = block.getMemory().begin >> 2;
         block.changeTimesUsed(1);
         this.blocks.mergeInt(block, 1, Integer::sum);
      } else {
         value = 0;
      }

      value /= PBlock.BYTE_SIZE / 4;
      if (skyBrightness != -1 && value != 0) {
         value |= skyBrightness << 13;
      }

      this.schematic.setEntry(x, y, z, value);
      this.dirty = true;
   }

   @Override
   public void allocate(MemoryManager memoryManager) {
      this.chunkMemory = memoryManager.allocate(this.getSize());
      loaded++;
   }

   @Override
   public void free(MemoryManager memoryManager) {
      if (this.chunkMemory != null) {
         memoryManager.free(this.chunkMemory);
         this.chunkMemory = null;
         loaded--;
      }
   }

   @Override
   public boolean update(MemoryManager memoryManager) {
      if (!this.dirty) {
         return false;
      }

      try {
         this.schematic.reset();
         this.schematic.initialize();
         this.schematic.optimizeThreaded().get();
         this.chunkMemory.getBuffer().asIntBuffer().put(this.schematic.getData());
         memoryManager.queueUpload(this);
      } catch (InterruptedException | ExecutionException e) {
         throw new RuntimeException(e);
      }

      this.dirty = false;
      return true;
   }

   @Override
   public void afterUpload() {
      this.dirty = false;
   }

   @Override
   public int getSize() {
      return 16384;
   }

   @Override
   public MemoryRegion getMemory() {
      return this.chunkMemory;
   }
}
