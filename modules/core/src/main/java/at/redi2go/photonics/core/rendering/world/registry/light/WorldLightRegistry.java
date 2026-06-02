package at.redi2go.photonics.core.rendering.world.registry.light;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.api.shaders.IShaderPack;
import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.registry.object.ObjectRegistry;
import at.redi2go.photonics.core.rendering.world.registry.object.WeakValue;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.ReadWriteLock;

public class WorldLightRegistry extends ObjectRegistry<WorldLight> {
    private final WorldAllocator allocator;

    public WorldLightRegistry(
            ReadWriteLock lock,
            WorldAllocator allocator
    ) {
        super(lock);
        this.allocator = allocator;
    }

    public @WeakValue WorldLight allocate(BlockLightInfo lightInfo, int blockId) {
        return cacheObject(
                new WorldLight(this, lightInfo, blockId),
                e -> e.allocate(allocator)
        );
    }

    public @Nullable @WeakValue WorldLight getWeak(IBlockState blockState) {
        var light = PhConfig.getLightRegistry().get(blockState);
        if (light == null) return null;

        var shaderPack = IShaderPack.getCurrentPack().orElse(null);
        if (shaderPack == null) return null;

        return allocate(light, shaderPack.getBlockId(blockState));
    }
}
