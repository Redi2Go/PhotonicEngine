package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.rendering.schematics.Schematic;
import at.redi2go.photonics.client.rendering.schematics.SchematicAlgorithms;
import at.redi2go.photonics.client.rendering.util.BufferUtils;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryOwner;
import at.redi2go.photonics.client.rendering.world.buffer.MemoryRegion;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;

public class PBlock implements MemoryOwner {
   public static int numAllocated = 0;
   public final int blockId;
   public int emissionColor;
   private Supplier<Schematic> compiledSchematicSupplier;
   private MemoryRegion blockMemory;
   private int timesUsed = 0;
   private boolean needsUpdate = false;
   private boolean schematicReady = false;
   private volatile boolean voxelRenderReady = false;
   private static final AtomicBoolean REBUILD_SCHEDULED = new AtomicBoolean();
   public boolean canOcclude = false;
   public static final int BLOCK_SIZE = 16;
   public static final int INT_SIZE = 4;
   public static final int SCHEMATIC_SIZE = 16384;
   public static int BYTE_SIZE = 16392;

   public PBlock(int blockId, Supplier<Schematic> compiledSchematicSupplier) {
      Objects.requireNonNull(compiledSchematicSupplier, "compiledSchematicSupplier must not be null");
      this.blockId = blockId;
      this.compiledSchematicSupplier = compiledSchematicSupplier;
   }

   public boolean needsUpdate() {
      return this.needsUpdate;
   }

   public boolean isUsed() {
      return this.timesUsed > 0;
   }

   public void changeTimesUsed(int delta) {
      this.timesUsed = Math.max(0, this.timesUsed + delta);
   }

   public boolean isAllocated() {
      return this.blockMemory != null;
   }

   public boolean isVoxelRenderReady() {
      return this.voxelRenderReady && this.blockMemory != null;
   }

   @Override
   public void allocate(MemoryManager memoryManager) {
      if (this.blockMemory == null) {
         this.blockMemory = memoryManager.allocate(this.getSize());
         this.needsUpdate = true;
         numAllocated++;
      }
   }

   @Override
   public void free(MemoryManager memoryManager) {
      if (this.blockMemory != null) {
         memoryManager.free(this.blockMemory);
         this.needsUpdate = true;
         this.blockMemory = null;
         numAllocated = Math.max(0, numAllocated - 1);
      }
   }

   @Override
   public boolean update(MemoryManager memoryManager) {
      if (!this.needsUpdate) {
         return false;
      }

      this.needsUpdate = false;
      Schematic schematic;
      if (this.compiledSchematicSupplier != null) {
         schematic = this.compiledSchematicSupplier.get();
         this.canOcclude = SchematicAlgorithms.canOcclude(schematic);
         this.compiledSchematicSupplier = null;
      } else {
         schematic = null;
      }

      IntBuffer buffer = this.blockMemory.getBuffer().asIntBuffer();
      buffer.position(0);
      buffer.put(this.blockId);
      buffer.put(this.emissionColor);
      if (schematic != null) {
         buffer.put(schematic.getData());
      }

      memoryManager.queueUploadPriority(this);
      return true;
   }

   @Override
   public void afterUpload() {
      if (this.schematicReady && !this.voxelRenderReady) {
         this.voxelRenderReady = true;
         if (REBUILD_SCHEDULED.compareAndSet(false, true)) {
            Minecraft.getInstance().execute(() -> {
               REBUILD_SCHEDULED.set(false);
               if (Minecraft.getInstance().levelRenderer != null) {
                  Minecraft.getInstance().levelRenderer.allChanged();
               }
            });
         }
      }
   }

   @Override
   public int getSize() {
      return BYTE_SIZE;
   }

   @Override
   public MemoryRegion getMemory() {
      return this.blockMemory;
   }

   public int getIndex() {
      return this.blockMemory.begin / (BYTE_SIZE / 8191);
   }

   public void setCompiledSchematicSupplier(Supplier<Schematic> compiledSchematicSupplier) {
      this.compiledSchematicSupplier = compiledSchematicSupplier;
      this.schematicReady = true;
      this.needsUpdate = true;
   }

   public void setEmissionColor(Vector3f color) {
      int[] bytes = BufferUtils.packUnorm4x8(color.x, color.y, color.z, 0.0F);
      this.emissionColor = bytes[0] | bytes[1] << 8 | bytes[2] << 16;
      this.needsUpdate = true;
   }
}
