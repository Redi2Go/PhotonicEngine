package at.redi2go.photonics.core.rendering.world.registry.block.contained;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockEntry;
import at.redi2go.photonics.core.rendering.world.block.ContainedBlockVoxel;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ContainedBlockFuture extends CompletableFuture<ContainedBlockVoxel> implements ContainedBlockEntry.Factory {
    @Override
    public ContainedBlockEntry createVariant(IntArraySet tint, int skylight, short region) {
        try {
            return get().createVariant(tint, skylight, region);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
