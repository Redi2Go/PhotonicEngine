package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.palette.TextureData;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface ContainedBlockEntry extends BlockEntry {
    interface Builder extends ContainedBlockEntry, BlockEntry.Builder, VoxelConsumer {
        void propagateException(Throwable ex);

        void setRegion(short region);

        @Override
        @Nullable ContainedBlockEntry build();
    }

    interface Factory {
        @Nullable ContainedBlockEntry createVariant(IntArraySet tint, int skylight, short region);
    }
}
