package at.redi2go.photonics.impl.mixins.mc.world.level;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;

@Mixin(IBlock.class)
@SuppressWarnings({"DataFlowIssue", "unchecked", "rawtypes"})
public interface IBlockImpl {
    @Overwrite
    static Optional<IBlock> fromId(Id id) {
        return (Optional) BuiltInRegistries.BLOCK
                .get((Identifier) (Object) id)
                .map(Holder.Reference::value);
    }
}
