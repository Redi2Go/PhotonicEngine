package at.redi2go.photonics.core.rendering.world.registry.block.template;

import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockModelImpl;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockPartImpl;
import at.redi2go.photonics.core.rendering.world.registry.objects.NoMemory;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;

import java.util.List;

public class BlockModelTemplate extends WorldObject<NoMemory> {
    private final long vertexHash;

    private final List<BlockPartTemplate> parts;
    private final ConcurrentLong2ObjectMap<BlockModelImpl> variants;

    public BlockModelTemplate(
            WorldRegistry registry,
            long vertexHash,
            List<BlockPartTemplate> parts
    ) {
        super(registry);

        this.vertexHash = vertexHash;

        this.parts = parts;
        this.variants = new ConcurrentLong2ObjectMap<>();

        setMemory(() -> NoMemory.INSTANCE);
    }

    @Override
    protected void loadDependants(List<WorldObject<?>> output) {
        for (var part : parts)
            part.loadDependants(output);
    }

    public BlockModelImpl createVariantWeak(
            TintBuilder.Result tint
    ) {
        return variants.computeIfAbsent(tint.hash(), (hash) -> new BlockModelImpl(
                worldRegistry,
                this,
                hash,
                parts.stream()
                        .map(e -> new BlockPartImpl(
                                e.createVariantWeak(worldRegistry, tint),
                                e
                        )).toList()
        ));
    }

    public void removeVariant(long hash) {
        variants.remove(hash);
    }

    @Override
    protected boolean dispose() {
        if (!super.dispose()) return false;

        worldRegistry.removeModelTemplate(vertexHash);
        return true;
    }
}
