package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.model.BlockModelRegistry;
import at.redi2go.photonics.core.rendering.world.registry.light.WorldLightRegistry;
import at.redi2go.photonics.core.rendering.world.registry.object.ObjectRegistry;
import at.redi2go.photonics.core.rendering.world.registry.palete.PaletteRegistry;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WorldRegistry {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final WorldLightRegistry lightRegistry;
    private final PaletteRegistry paletteRegistry;

    private final BlockRegistry blockRegistry;
    private final BlockModelRegistry blockModelRegistry;

    public WorldRegistry(
            WorldAllocator worldAllocator,
            PaletteTexture paletteTexture,
            AtlasDownloader atlasDownloader
    ) {
        var bakery = BlockBakery.newBakery(atlasDownloader);

        this.lightRegistry = new WorldLightRegistry(lock, worldAllocator);
        this.paletteRegistry = new PaletteRegistry(lock, paletteTexture);

        this.blockRegistry = new BlockRegistry(lock, worldAllocator, paletteRegistry);
        this.blockModelRegistry = new BlockModelRegistry(lock, bakery, blockRegistry);
    }

    public WorldLightRegistry lightRegistry() {
        return lightRegistry;
    }

    public BlockModelRegistry blockModelRegistry() {
        return blockModelRegistry;
    }

    public void freeUnusedObjects() throws InterruptedException {
        lock.writeLock().lockInterruptibly();

        ObjectRegistry<?>[] registries = new ObjectRegistry[]{
                lightRegistry,
                paletteRegistry,
                blockRegistry,
                blockModelRegistry
        };

        try {
            while (hasEnqueuedObject(registries)) {
                lightRegistry.freeUnusedObjects();
                paletteRegistry.freeUnusedObjects();
                blockRegistry.freeUnusedObjects();
                blockModelRegistry.freeUnusedObjects();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static boolean hasEnqueuedObject(ObjectRegistry<?>[] registries) {
        for (int i = 0; i < registries.length; i++) {
            if (registries[i].hasEnqueuedObject())
                return true;
        }

        return false;
    }
}
