package at.redi2go.photonics.core.rendering.world.block;

import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import org.jetbrains.annotations.Nullable;

public interface BlockProvider {
    @Nullable BlockEntry createVariant(TintBuilder.Result tintInfo, int skylight, short region);
}
