package at.redi2go.photonics.common.mixins.iris.pipeline.passes.composite;

import at.redi2go.photonics.common.iris.pipeline.CompositeRendererPassExt;
import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeRenderer.class)
public interface CompositeRendererAccessor {
    @Accessor("passes")
    ImmutableList<CompositeRendererPassExt> getPasses();
}
