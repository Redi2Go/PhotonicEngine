package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import at.redi2go.photonics.core.config.Variable;
import at.redi2go.photonics.core.config.lights.block.LightBlock;
import at.redi2go.photonics.core.config.lights.predicate.LightPredicate;
import at.redi2go.photonics.core.iris.AbstractIrisPackLights;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.BlockEntry;
import net.irisshaders.iris.shaderpack.materialmap.TagEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class IrisPackLightsImpl extends AbstractIrisPackLights {
    private ShaderPack shaderPack;
    private Set<Block> usedBlocks;

    public void setShaderPack(ShaderPack pack) {
        this.shaderPack = pack;
        defines.setOwner(this);
    }

    private void loadUsedBlocks() {
        if (usedBlocks != null) return;

        usedBlocks = new HashSet<>();
        final var idMap = shaderPack.getIdMap();

        for (var entry : idMap.getBlockProperties().values()) {
            for (var blockId : entry) {
                Identifier identifier = Identifier.fromNamespaceAndPath(blockId.id().getNamespace(), blockId.id().getName());

                BuiltInRegistries.BLOCK.get(identifier)
                        .ifPresent(e -> usedBlocks.add(e.value()));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
        if (type == LightBlock.TYPE) {
            try {
                final var idMap = shaderPack.getIdMap();

                int id = Integer.parseInt(name);

                var blocks = idMap.getBlockProperties().getOrDefault(id, null);
                var tags = idMap.getTagEntries().getOrDefault(id, null);

                if (blocks == null && tags == null)
                    return Optional.empty();

                return (Optional<T>) Optional.of(new ShaderLightBlock(blocks, tags));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return defines.getValue(type, name);
    }

    private class ShaderLightBlock implements LightBlock {
        private final @Nullable List<BlockEntry> blocks;
        private final @Nullable List<TagEntry> tags;

        private ShaderLightBlock(
                @Nullable List<BlockEntry> blocks,
                @Nullable List<TagEntry> tags
        ) {
            this.blocks = blocks;
            this.tags = tags;
        }

        @Override
        public List<LightPredicate> listPredicates() {
            loadUsedBlocks();
            final var out = new ArrayList<LightPredicate>();

            if (blocks != null) {
                for (var block : blocks) {
                    var id = Identifier.fromNamespaceAndPath(block.id().getNamespace(), block.id().getName());
                    BuiltInRegistries.BLOCK.get(id)
                            .ifPresent(e -> {
                                out.add(
                                        new ShaderPredicate(
                                                e.value(),
                                                block.propertyPredicates()
                                        )
                                );
                            });
                }
            }

            if (tags != null) {
                for (var tag : tags) {
                    var id = Identifier.fromNamespaceAndPath(tag.id().getNamespace(), tag.id().getName());
                    var key = TagKey.create(Registries.BLOCK, id);

                    BuiltInRegistries.BLOCK.get(key)
                            .ifPresent(blocks -> {
                                for (var block : blocks) {
                                    if (usedBlocks.contains(block.value())) continue;

                                    out.add(new ShaderPredicate(
                                            block.value(),
                                            tag.propertyPredicates()
                                    ));
                                }
                            });
                }
            }

            return out;
        }

        public @Nullable List<BlockEntry> blocks() {
            return blocks;
        }

        public @Nullable List<TagEntry> tags() {
            return tags;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ShaderLightBlock) obj;
            return Objects.equals(this.blocks, that.blocks) &&
                    Objects.equals(this.tags, that.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blocks, tags);
        }

        @Override
        public String toString() {
            return "ShaderLightBlock[" +
                    "blocks=" + blocks + ", " +
                    "tags=" + tags + ']';
        }

    }

    private record ShaderPredicate(
            Block mcBlock,
            Map<String, String> vagueProperties
    ) implements LightPredicate {
        public ShaderPredicate {
            Objects.requireNonNull(mcBlock, "block was null");
            Objects.requireNonNull(vagueProperties, "vagueProperties was null");
        }

        @Override
        public IBlock block() {
            return (IBlock) mcBlock;
        }

        @Override
        public int priority() {
            return LightPredicate.NORMAL_PRIORITY;
        }

        @Override
        public boolean test(IBlockPos pos, ILevelReader levelReader) {
            final BlockState state = (BlockState) levelReader.ph$getBlockState(pos);
            if (!state.is(mcBlock)) return false;

            for (Map.Entry<String, String> entry : vagueProperties.entrySet()) {
                final Property<?> property = mcBlock.getStateDefinition().getProperty(entry.getKey());
                if (property == null) return false;

                final var value = property.getValue(entry.getValue()).orElse(null);
                if (value == null) return false;

                if (!value.equals(state.getValue(property))) return false;
            }

            return true;
        }
    }

}
