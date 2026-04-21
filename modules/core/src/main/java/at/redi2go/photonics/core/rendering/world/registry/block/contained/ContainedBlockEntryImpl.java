package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.registry.block.regular.RegularBlockBuilder;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class ContainedBlockEntryImpl implements ContainedBlockEntry {
    private final short region;
    private final ContainedBlockVariant variant;

    public ContainedBlockEntryImpl(short region, ContainedBlockVariant variant) {
        this.region = region;
        this.variant = variant;

        variant.acquire();
    }

    @Override
    public int begin() {
        return variant.begin();
    }

    @Override
    public int skylight() {
        return variant.skylight();
    }

    @Override
    public BlockEntry.Builder createBuilder() {
        var builder = new RegularBlockBuilder(variant.registry());
        builder.initRegion(region);
        builder.load(variant, ShortSet.of());

        return builder;
    }

    @Override
    public @Nullable BlockEntry.Builder clearRegions(ShortSet regions) {
        if (regions.contains(region)) return null;

        return createBuilder();
    }

    @Override
    public void close() {
        variant.close();
    }
}
