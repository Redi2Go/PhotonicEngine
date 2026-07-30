package at.redi2go.photonics.client.rendering.opengl.rendering.renderers;

import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import at.redi2go.photonics.client.rendering.opengl.objects.GLMemoryCollection;
import at.redi2go.photonics.client.rendering.opengl.objects.TextureObject;
import at.redi2go.photonics.client.rendering.opengl.rendering.PhotonicsShader;
import at.redi2go.photonics.client.rendering.world.WorldRegistry;
import at.redi2go.photonics.client.rendering.world.buffer.GlMemoryManager;
import at.redi2go.photonics.client.rendering.world.buffer.GlProgramBuffer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import org.jetbrains.annotations.Nullable;

public abstract class MainRenderer implements Destructable {
   protected final WorldRegistry worldRegistry;
   protected final float renderScale;
   protected final GLMemoryCollection memoryCollection;
   protected final Int2ObjectMap<List<GlProgramBuffer>> foundMemories;

   public MainRenderer(WorldRegistry worldRegistry, float renderScale) {
      this.worldRegistry = worldRegistry;
      this.renderScale = renderScale;
      this.memoryCollection = this.buildGlMemoryCollection();
      this.foundMemories = new Int2ObjectOpenHashMap();
   }

   public abstract void createCompositeRenderer(Function<@Nullable PhotonicsShader[], CompositeRenderer> var1);

   protected void addTextureSampler(SamplerHolder samplers, String name, Supplier<TextureObject> textureObjectSupplier) {
      samplers.addDynamicSampler(() -> {
         TextureObject textureObject = textureObjectSupplier.get();
         textureObject.updatePerFrame();
         return textureObject.getTextureId();
      }, null, new String[]{name});
   }

   public abstract void registerCustomTextures(SamplerHolder var1);

   public abstract void registerCustomUniforms(DynamicUniformHolder var1);

   public abstract void render();

   public abstract void recalculateSizes();

   public void setup() {
      while (!this.worldRegistry.getGlQueue().isEmpty()) {
         Objects.requireNonNull(this.worldRegistry.getGlQueue().poll()).run();
      }

      this.worldRegistry.upload();
   }

   public void bindProgramBuffers(int shaderId, IntSet usedBuffers) {
      int bindingPointIndex = 16;

      for (GlProgramBuffer buffer : (List<GlProgramBuffer>)this.foundMemories.computeIfAbsent(shaderId, id -> {
         ArrayList<GlProgramBuffer> memories = new ArrayList<>();

         for (Supplier<GlMemoryManager> glMemoryManagerSupplier : this.memoryCollection) {
            GlMemoryManager glMemoryManager = glMemoryManagerSupplier.get();
            GlProgramBuffer memory = glMemoryManager.findInProgram(id);
            if (memory != null) {
               memories.add(memory);
            }
         }

         return memories;
      })) {
         while (usedBuffers.contains(--bindingPointIndex)) {
         }

         buffer.bind(bindingPointIndex);
      }
   }

   protected GLMemoryCollection buildGlMemoryCollection() {
      GLMemoryCollection memoryCollection = new GLMemoryCollection();
      memoryCollection.add(this.worldRegistry::getRootMemoryManager);
      memoryCollection.add(this.worldRegistry::getCbMemoryManager);
      memoryCollection.add(() -> this.worldRegistry.getLightRegistry().getLightsMemoryManager());
      if (this.worldRegistry.getLightRegistry().isLightBinningEnabled()) {
         memoryCollection.add(() -> this.worldRegistry.getLightRegistry().getRegistryMemoryManager());
      }

      memoryCollection.add(() -> this.worldRegistry.getLightRegistry().getLightMappingMemoryManager());
      return memoryCollection;
   }
}
