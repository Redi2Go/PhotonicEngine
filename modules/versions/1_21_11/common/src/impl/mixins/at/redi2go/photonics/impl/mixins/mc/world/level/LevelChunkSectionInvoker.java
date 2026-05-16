package at.redi2go.photonics.impl.mixins.mc.world.level;

import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelChunkSection.class)
public interface LevelChunkSectionInvoker {
    @Invoker("<init>")
    static LevelChunkSection copySection(LevelChunkSection toCopy) {
        throw new AssertionError();
    }
}
