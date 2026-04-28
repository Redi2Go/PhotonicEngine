package at.redi2go.photonics.common.meshing;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockSetBuilder {
    private final ImmutableSet.Builder<Block> blocks = ImmutableSet.builder();

    public BlockSetBuilder addBlock(Block block) {


        blocks.add(block);

        return this;
    }

    public BlockSetBuilder addTag(TagKey<Block> blockTagKey) {
        BuiltInRegistries.BLOCK.get(blockTagKey)
                .ifPresent((blocks) ->
                        blocks.forEach(holder ->
                                this.blocks.add(holder.value())));

        return this;
    }

    public Set<Block> build() {
        return blocks.build();
    }
}
