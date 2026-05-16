package at.redi2go.photonics.core.rendering.world.registry.block.template;

import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.ManagedObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockModelImpl;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockPartImpl;

import java.util.List;

public class BlockModelTemplate extends ManagedObject<BlockModelTemplate> implements BlockProvider {
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

        acquireDependants();
        parts.forEach(e -> e.setModelTemplate(this));
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {

    }

    @Override
    protected BlockModelTemplate getWrappedValue() {
        return this;
    }

    @Override
    public BlockModel createVariant(
            TintBuilder.Result tint
    ) {
        return variants.computeIfAbsent(tint.hash(), (hash) -> new BlockModelImpl(
                registry,
                makeManagedRef(),
                hash,
                parts.stream()
                        .map(e -> new BlockPartImpl(
                                e.createVariant(tint),
                                e.makeManagedRef()
                        ))
                        .toList()
        ));
    }

    public void removeVariant(long hash) {
        variants.remove(hash);
    }

    @Override
    protected boolean dispose() {
        if (!super.dispose()) return false;

        registry.removeBlockProvider(vertexHash);
        return true;
    }
}
