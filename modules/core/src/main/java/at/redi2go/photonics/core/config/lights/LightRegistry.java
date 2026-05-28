package at.redi2go.photonics.core.config.lights;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.mc.world.level.ILevelReader;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class LightRegistry {
    private final ListMultimap<IBlock, BlockLightInfo> lights = ArrayListMultimap.create();

    public Set<IBlock> keySet() {
        return lights.keySet();
    }

    public void clear() {
        lights.clear();
    }

    public void add(BlockLightInfo light) {
        lights.put(light.block(), light);
    }

    public void sort() {
        for (final var key : lights.keySet())
            lights.get(key)
                    .sort(Comparator.reverseOrder()); // Highest priority gets checked first
    }

    public @Nullable BlockLightInfo get(IBlockPos pos, ILevelReader level) {
        final List<BlockLightInfo> values = lights.get(level.ph$getBlockState(pos).ph$block());
        for (var light : values) {
            if (light.intensity() == 0f) return null;
            if (light.emitsLight(pos, level)) return light;
        }

        return null;
    }

    public @Nullable BlockLightInfo get(IBlockState blockState) {
        return get(IBlockPos.zero(), ILevelReader.createFacade(blockState));
    }

    public @Nullable BlockLightInfo getDefault(IBlock block) {
        return get(block.ph$defaultBlockState());
    }

    /**
     * Returns true if any states for <code>block</code> are traced, ignores {@link PhotonicsConfig#getTracedOverrides()}.
     */
    public boolean isTraced(IBlock block) {
        final List<BlockLightInfo> values = lights.get(block);
        for (var light : values)
            if (light.requestedTrace())
                return true;

        return false;
    }

    @Override
    public int hashCode() {
        return lights.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LightRegistry other)) return false;

        return other.lights.equals(lights);
    }
}
