package at.redi2go.photonics.core.rendering.world.allocator.block;

import at.redi2go.photonics.core.rendering.world.RegionMapping;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

public class BlockEntryImpl extends RegionMapping implements BlockEntry {
    private final BlockEntryData data;

    public BlockEntryImpl(RegionMapping regions, BlockEntryData data) {
        super(regions);

        this.data = data;
    }

    @Override
    public int begin() {
        return data.begin();
    }

    @Override
    public int skylight() {
        return data.skylight();
    }

    @Override
    public Builder createBuilder() {
        var builder = new BlockEntryBuilderImpl(data.allocator(), this);
        builder.load(data, ShortSet.of());

        return builder;
    }

    @Override
    public @Nullable Builder clearRegions(ShortSet regions) {
        return null;
    }

    @Override
    public void acquire() {
        data.acquire();
    }

    @Override
    public void release() {
        data.release();
    }
}
