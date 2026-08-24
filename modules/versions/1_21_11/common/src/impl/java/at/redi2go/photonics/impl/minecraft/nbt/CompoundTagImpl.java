package at.redi2go.photonics.impl.minecraft.nbt;

import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CompoundTag.class)
public abstract class CompoundTagImpl implements ICompoundTag {
    @Shadow
    public abstract boolean isEmpty();

    @Override
    public boolean ph$isEmpty() {
        return isEmpty();
    }

    @Mixin(ICompoundTag.class)
    public interface StaticMethods {
        @Overwrite
        @SuppressWarnings("DataFlowIssue")
        static boolean isEqual(ICompoundTag tag1, ICompoundTag tag2) {
            return NbtUtils.compareNbt((CompoundTag) (Object) tag1, (CompoundTag) (Object) tag2, true);
        }
    }

}
