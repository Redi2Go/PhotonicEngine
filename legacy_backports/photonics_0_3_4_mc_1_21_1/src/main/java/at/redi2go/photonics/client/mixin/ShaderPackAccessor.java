package at.redi2go.photonics.client.mixin;

import java.util.function.Function;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ShaderPack.class, remap = false)
public interface ShaderPackAccessor {
   @Accessor("sourceProvider")
   Function<AbsolutePackPath, String> getSourceProvider();

   @Accessor("shaderProperties")
   ShaderProperties getShaderProperties();
}
