package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import at.redi2go.photonics.core.rendering.world.allocator.WorldLightMemory;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;

public class BlockLightOwner extends WorldObject<WorldLightMemory> {
    private final BlockLightInfo light;
    private final int blockId;

    public BlockLightOwner(
            WorldRegistry worldRegistry,
            BlockLightInfo light,
            int blockId
    ) {
        super(worldRegistry);

        this.light = light;
        this.blockId = blockId;
    }

    public int entryData() {
        return memoryOrThrow().entryData();
    }

    public void allocate() {
        var memory = setMemory(() -> worldRegistry.worldAllocator().allocateWorldLight());

        memory.setLight(light, blockId);
        memory.upload();
    }

    public BlockLightInfo lightInfo() {
        return light;
    }

    public int blockId() {
        return blockId;
    }

    @Override
    public int hashCode() {
        return light.hashCode() ^ blockId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockLightOwner other && other.blockId == blockId && other.light.equals(light);
    }
}
