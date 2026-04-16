package at.redi2go.photonics.core.rendering.world.bakery;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Optional;

public interface BlockMesher {
    Registry REGISTRY = new Registry();

    /**
     * Meshes a block at {@code pos} with {@code blockState}.
     *
     * @apiNote {@code VertexBuilder} only accepts quads
     */
    void meshBlock(
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter,
            VertexBuilder vertexBuilder
    );

    class Registry {
        private final Object2ObjectMap<Id, BlockMesher> blockRegistry = new Object2ObjectOpenHashMap<>();
        private final Object2ObjectMap<String, BlockMesher> namespaceRegistry = new Object2ObjectOpenHashMap<>();
        private BlockMesher defaultMesher = null;

        Registry() {

        }

        public void addBlock(Id id, BlockMesher mesher) {
            blockRegistry.putIfAbsent(id, mesher);
        }

        public void addNamespace(String namespace, BlockMesher mesher) {
            namespaceRegistry.putIfAbsent(namespace, mesher);
        }

        public void addDefault(BlockMesher mesher) {
            defaultMesher = mesher;
        }

        public Optional<BlockMesher> get(IBlock block) {
            return Optional.ofNullable(blockRegistry.get(block.id()))
                    .or(() -> Optional.of(namespaceRegistry.get(block.id().namespace())))
                    .or(() -> Optional.ofNullable(defaultMesher));
        }
    }
}
