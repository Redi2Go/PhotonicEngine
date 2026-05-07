package at.redi2go.photonics.impl.mixins.mc.blaze3d.common.textures;

import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import com.mojang.blaze3d.textures.FilterMode;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FilterMode.class)
@Implements(@Interface(iface = IFilterMode.class, prefix = "ph$"))
public abstract class FilterModeMixin {

}
