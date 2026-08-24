package at.redi2go.photonics.impl.minecraft.world.level.chunk;

import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelChunkSection.class)
public abstract class ChunkSectionImpl implements IChunkSection {
    @Shadow
    public abstract BlockState getBlockState(int i, int j, int k);

    @Shadow
    public abstract boolean hasOnlyAir();

    @Override
    public IBlockState ph$getBlockState(int x, int y, int z) {
        return (IBlockState) getBlockState(x, y, z);
    }

    @Override
    public boolean ph$hasOnlyAir() {
        return hasOnlyAir();
    }

    @Override
    public IChunkSection ph$createCopy() {
        return (IChunkSection) CopyInvoker.copySection((LevelChunkSection) (Object) this);
    }

    @Mixin(LevelChunkSection.class)
    public interface CopyInvoker {
        @Invoker("<init>")
        static LevelChunkSection copySection(LevelChunkSection toCopy) {
            throw new AssertionError();
        }
    }

    @Mixin(DataLayer.class)
    public static abstract class LightDataImpl implements ILightData {
        @Shadow
        public abstract int get(int i, int j, int k);

        @Shadow
        protected abstract int get(int i);

        @Override
        public int ph$getLightLevel(int x, int y, int z) {
            return get(x, y, z);
        }

        @Override
        public int ph$getLightLevel(int index) {
            return get(index);
        }
    }
}
