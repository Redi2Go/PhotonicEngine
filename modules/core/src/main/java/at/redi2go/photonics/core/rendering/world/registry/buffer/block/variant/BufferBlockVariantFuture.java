package at.redi2go.photonics.core.rendering.world.registry.buffer.block.variant;

import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.BufferBlockVoxel;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BufferBlockVariantFuture extends CompletableFuture<BufferObject.@Nullable ManagedRef<BufferBlockVoxel.Variant>> implements BlockProvider {
    @Override
    public BlockEntry createVariant(TintBuilder.Result tintInfo, int skylight, short region) {
        try {
            var result = get();
            if (result == null) return null;

            return result.get().createVariant(tintInfo, skylight, region);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
