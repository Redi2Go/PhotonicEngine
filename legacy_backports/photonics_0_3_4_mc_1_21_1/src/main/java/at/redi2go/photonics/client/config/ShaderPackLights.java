package at.redi2go.photonics.client.config;

import at.redi2go.photonics.client.config.lights.LightDefines;
import at.redi2go.photonics.client.config.lights.LightGroup;
import at.redi2go.photonics.client.config.lights.LightList;
import at.redi2go.photonics.client.config.lights.LightsProvider;
import at.redi2go.photonics.client.config.lights.block.LightBlock;
import at.redi2go.photonics.client.config.lights.predicate.LightPredicate;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.irisshaders.iris.shaderpack.IdMap;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.BlockEntry;
import net.irisshaders.iris.shaderpack.materialmap.TagEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class ShaderPackLights implements LightsProvider, Variable.Owner {
   LightDefines defines = LightDefines.EMPTY;
   LinkedHashMap<String, LightGroup> lights = new LinkedHashMap<>(0);
   private ShaderPack shaderPack;
   private Set<Block> usedBlocks;

   public void setShaderPack(ShaderPack pack) {
      this.shaderPack = pack;
      this.defines.setOwner(this);
   }

   private void loadUsedBlocks() {
      if (this.usedBlocks == null) {
         this.usedBlocks = new HashSet<>();
         IdMap idMap = this.shaderPack.getIdMap();
         ObjectIterator var2 = idMap.getBlockProperties().values().iterator();

         while (var2.hasNext()) {
            List<BlockEntry> entry = (List<BlockEntry>)var2.next();

            for (BlockEntry blockId : entry) {
               ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath(blockId.id().getNamespace(), blockId.id().getName());
               BuiltInRegistries.BLOCK.getOptional(identifier).ifPresent(e -> this.usedBlocks.add(e));
            }
         }
      }
   }

   @Override
   public void registerLights(LightList lights) {
      for (LightGroup lightGroup : this.lights.values()) {
         lightGroup.recordLights(this, lights, PhotonicsConfig.getTracedOverrides(), 1000);
      }
   }

   @Override
   public void registerChangeListener(Runnable consumer) {
   }

   @Override
   public void clearListeners() {
   }

   @Override
   public int mod() {
      return 0;
   }

   @Override
   public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
      if (type == LightBlock.TYPE) {
         try {
            IdMap idMap = this.shaderPack.getIdMap();
            int id = Integer.parseInt(name);
            List<BlockEntry> blocks = (List<BlockEntry>)idMap.getBlockProperties().getOrDefault(id, null);
            List<TagEntry> tags = (List<TagEntry>)idMap.getTagEntries().getOrDefault(id, null);
            return blocks == null && tags == null ? Optional.empty() : Optional.of((T)(new ShaderPackLights.ShaderLightBlock(blocks, tags)));
         } catch (NumberFormatException e) {
            return Optional.empty();
         }
      } else {
         return this.defines.getValue(type, name);
      }
   }

   public static ShaderPackLights parse(String contents) {
      String[] lines = contents.split("\n");
      int start = 0;

      for (int i = lines.length - 1; i >= 0; i--) {
         String line = lines[i];
         if (!line.isEmpty() && line.charAt(0) == '{') {
            start = i;
            break;
         }
      }

      contents = String.join("\n", Arrays.copyOfRange(lines, start, lines.length));
      contents = contents.replaceAll("`", "\"");
      return (ShaderPackLights)PhotonicsConfig.GSON.fromJson(contents, ShaderPackLights.class);
   }

   private class ShaderLightBlock implements LightBlock {
      @Nullable
      private final List<BlockEntry> blocks;
      @Nullable
      private final List<TagEntry> tags;

      private ShaderLightBlock(@Nullable List<BlockEntry> blocks, @Nullable List<TagEntry> tags) {
         this.blocks = blocks;
         this.tags = tags;
      }

      @Override
      public List<LightPredicate> listPredicates() throws CommandSyntaxException {
         ShaderPackLights.this.loadUsedBlocks();
         ArrayList<LightPredicate> out = new ArrayList<>();
         if (this.blocks != null) {
            for (BlockEntry block : this.blocks) {
               ResourceLocation id = ResourceLocation.fromNamespaceAndPath(block.id().getNamespace(), block.id().getName());
               BuiltInRegistries.BLOCK.getOptional(id).ifPresent(e -> out.add(new ShaderPackLights.ShaderPredicate(e, block.propertyPredicates())));
            }
         }

         if (this.tags != null) {
            for (TagEntry tag : this.tags) {
               ResourceLocation id = ResourceLocation.fromNamespaceAndPath(tag.id().getNamespace(), tag.id().getName());
               TagKey<Block> key = TagKey.create(Registries.BLOCK, id);
               BuiltInRegistries.BLOCK.getTag(key).ifPresent(blocks -> {
                  for (Holder<Block> block : blocks) {
                     if (!ShaderPackLights.this.usedBlocks.contains(block.value())) {
                        out.add(new ShaderPackLights.ShaderPredicate((Block)block.value(), tag.propertyPredicates()));
                     }
                  }
               });
            }
         }

         return out;
      }

      @Nullable
      public List<BlockEntry> blocks() {
         return this.blocks;
      }

      @Nullable
      public List<TagEntry> tags() {
         return this.tags;
      }

      @Override
      public boolean equals(Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && obj.getClass() == this.getClass()) {
            ShaderPackLights.ShaderLightBlock that = (ShaderPackLights.ShaderLightBlock)obj;
            return Objects.equals(this.blocks, that.blocks) && Objects.equals(this.tags, that.tags);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.blocks, this.tags);
      }

      @Override
      public String toString() {
         return "ShaderLightBlock[blocks=" + this.blocks + ", tags=" + this.tags + "]";
      }
   }

   private record ShaderPredicate(Block block, Map<String, String> vagueProperties) implements LightPredicate {
      public ShaderPredicate {
         Objects.requireNonNull(block, "block was null");
         Objects.requireNonNull(vagueProperties, "vagueProperties was null");
      }

      @Override
      public int priority() {
         return 0;
      }

      @Override
      public boolean test(BlockInWorld block) {
         BlockState state = block.getState();
         if (!state.is(this.block())) {
            return false;
         }

         for (Entry<String, String> entry : this.vagueProperties.entrySet()) {
            Property<?> property = this.block().getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
               return false;
            }

            Comparable<?> value = (Comparable<?>)property.getValue(entry.getValue()).orElse(null);
            if (value == null) {
               return false;
            }

            if (!value.equals(state.getValue(property))) {
               return false;
            }
         }

         return true;
      }
   }
}
