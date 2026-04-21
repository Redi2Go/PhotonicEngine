package at.redi2go.photonics.core.rendering.world.registry.block.regular;

import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class RegularBlockEntry extends RegionMapping implements BlockEntry {
    private final RegularBlockVariant variant;

    public RegularBlockEntry(RegionMapping regions, RegularBlockVariant variant) {
        super(regions);

        this.variant = variant;
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
    public Builder createBuilder() {
        var builder = new RegularBlockBuilder(variant.registry(), this);
        builder.load(variant, ShortSet.of());

        return builder;
    }

    @Override
    public @Nullable Builder clearRegions(ShortSet regions) {
        var builder = new RegularBlockBuilder(variant.registry(), this);
        return builder.load(variant, regions) ? null : builder;
    }

    @Override
    public void close() {
        variant.close();
    }
}
