package at.redi2go.photonics.client.mixin;

import java.util.List;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReloadableResourceManager.class)
public interface ReloadableResourceManagerAccessor {
   @Accessor("listeners")
   List<PreparableReloadListener> getListeners();
}
