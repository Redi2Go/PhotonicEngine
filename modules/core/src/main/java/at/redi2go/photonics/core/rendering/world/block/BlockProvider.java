package at.redi2go.photonics.core.rendering.world.block;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.Nullable;

public interface BlockProvider {
    @Nullable BlockEntry createVariant(IntArraySet tint, int skylight, short region);
}
