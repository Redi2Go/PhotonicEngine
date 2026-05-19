package at.redi2go.photonics.core.rendering.world.registry;

import at.redi2go.photonics.api.mc.core.IBlockPos;
import at.redi2go.photonics.api.mc.world.level.IBlockAndTintGetter;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import at.redi2go.photonics.core.rendering.RenderingComponent;
import at.redi2go.photonics.core.rendering.world.allocator.WorldAllocator;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBakery;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMeshState;
import at.redi2go.photonics.core.rendering.world.bakery.BlockMesher;
import at.redi2go.photonics.core.rendering.world.bakery.texture.AtlasDownloader;
import at.redi2go.photonics.core.rendering.world.block.BlockModel;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteTexture;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockHeader;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockModelImpl;
import at.redi2go.photonics.core.rendering.world.registry.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.block.builder.BlockModelBuilder;
import at.redi2go.photonics.core.rendering.world.registry.block.template.BlockModelTemplate;
import at.redi2go.photonics.core.rendering.world.registry.objects.ObjectManager;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;
import at.redi2go.photonics.core.rendering.world.registry.optimization.OptimizationService;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldRegistry implements RenderingComponent {
    private final ObjectManager objectManager = new ObjectManager();

    private final WorldAllocator worldAllocator;
    private final PaletteTexture paletteTexture;

    private final BlockBakery blockBakery;
    private final OptimizationService optimizationService;


    private final ConcurrentHashMap<Object, WorldObject> objectCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<BlockMeshState, CompletableFuture<@Nullable BlockModel>> blockModelCache = new ConcurrentHashMap<>();
    private final ConcurrentLong2ObjectMap<CompletableFuture<BlockModelTemplate>> modelTemplateCache = new ConcurrentLong2ObjectMap<>(16);

    public WorldRegistry(
            WorldAllocator worldAllocator,
            PaletteTexture paletteTexture,
            AtlasDownloader atlasDownloader,
            OptimizationService optimizationService
    ) {
        this.worldAllocator = worldAllocator;
        this.paletteTexture = paletteTexture;

        this.blockBakery = BlockBakery.newBakery(atlasDownloader);
        this.optimizationService = optimizationService;
    }

    public ObjectManager objectManager() {
        return objectManager;
    }

    public WorldAllocator worldAllocator() {
        return worldAllocator;
    }

    public PaletteTexture paletteTexture() {
        return paletteTexture;
    }

    public OptimizationService optimizationService() {
        return optimizationService;
    }


    @SuppressWarnings("unchecked")
    private <T extends WorldObject<?>, K> T cacheObjectWeak(
            K key,
            Function<K, T> supplier,
            Consumer<T> allocator
    ) {
        var value = objectCache.get(key);
        if (value != null) return (T) value;

        var newValue = supplier.apply(key);
        var result = objectCache.putIfAbsent(newValue, newValue);
        if (result == null) {
            allocator.accept(newValue);
            return newValue;
        }

        return (T) result;
    }

    public void removeObject(WorldObject<?> object) {
        objectCache.remove(object);
    }


    public PaletteObject allocatePaletteWeak(PaletteEntry entry) {
        entry.computeHashCode();

        return cacheObjectWeak(
                entry,
                e -> new PaletteObject(this, e),
                PaletteObject::allocate
        );
    }

    public BlockVoxel allocateBlockVoxelWeak(long hash, int[] data) {
        return cacheObjectWeak(
                new BlockVoxel(this, hash),
                e -> e,
                e -> e.allocate(data)
        );
    }

    public BlockHeader allocateBlockHeaderWeak(
            BlockLightOwner blockLight,
            int[] tint,
            List<PaletteObject> weakPalette,
            BlockVoxel weakBlockVoxel,
            long voxelHash,
            long tintHash
    ) {
        return cacheObjectWeak(
                new BlockHeader(this, blockLight, tint, weakPalette, weakBlockVoxel, voxelHash, tintHash),
                e -> e,
                BlockHeader::allocate
        );
    }

    public BlockLightOwner allocateBlockLightWeak(BlockLightInfo lightInfo, int blockId) {
        return cacheObjectWeak(
                new BlockLightOwner(this, lightInfo, blockId),
                e -> e,
                BlockLightOwner::allocate
        );
    }

    public void removeModelTemplate(long vertexHash) {
        modelTemplateCache.remove(vertexHash);
    }

    public void removeBlockModel(BlockMeshState blockMeshState) {
        blockModelCache.remove(blockMeshState);
    }

    private CompletionStage<BlockModelTemplate> cacheModelTemplateWeak(BlockBakery.MeshResult blockMesh) {
        CompletableFuture<BlockModelTemplate> future = new CompletableFuture<>();
        var resultFuture = modelTemplateCache.putIfAbsent(blockMesh.vertexHash(), future);

        if (resultFuture != null) return resultFuture;

        try {
            var builder = new BlockModelBuilder(this, blockMesh.vertexHash(), blockMesh.tintData());

            blockMesh.bake(builder);
            blockMesh.close();

            future.complete(builder.build());
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        blockMesh.close();

        return future;
    }

    public <T extends BlockMeshState> CompletionStage<@Nullable BlockModel> getBlockModel(
            BlockMesher<T> blockMesher,
            Vector3i blockChunkOffset,
            IBlockPos pos,
            IBlockState blockState,
            IBlockAndTintGetter blockAndTintGetter
    ) {
        T meshState = blockMesher.extractMeshState(
                blockChunkOffset,
                pos,
                blockState,
                blockAndTintGetter
        );

        CompletableFuture<@Nullable BlockModel> future = new CompletableFuture<>();

        try (var lock = objectManager.acquireLock()) {
            if (meshState.shouldCache()) {
                var resultFuture = blockModelCache.putIfAbsent(meshState, future);
                if (resultFuture != null)
                    return acquireModelReference(resultFuture);
            }

            var meshResult = blockBakery.meshBlock(
                    blockMesher,
                    meshState,
                    blockChunkOffset,
                    pos,
                    blockState,
                    blockAndTintGetter
            );

            meshState.prepareCacheUse();
            if (meshResult == null) {
                future.complete(null);
                return future;
            }

            TintBuilder.Result tintInfo = meshResult.tintData();
            cacheModelTemplateWeak(meshResult)
                    .handle((template, e) -> {
                        try {
                            if (e != null) {
                                future.completeExceptionally(e);
                            } else {
                                var variant = template.createVariantWeak(blockState, tintInfo);
                                variant.addMeshState(meshState);

                                future.complete(variant);
                            }
                        } catch (Throwable t) {
                            future.completeExceptionally(t);
                        }

                        return null;
                    });

            return acquireModelReference(future);
        } catch (Throwable t) {
            future.completeExceptionally(t);
            return future;
        }
    }

    private CompletionStage<@Nullable BlockModel> acquireModelReference(CompletionStage<@Nullable BlockModel> originalFuture) {
        // Acquiring references is safe in thenApply as its either completed now, in which case we already hold the lock
        // or its being voxelized by a thread which also holds the lock, which will process all the thenApply dependants on that thread
        return originalFuture.thenApply((e) -> {
            if (e != null)
                ((BlockModelImpl) e).acquireReference();

            return e;
        });
    }
}
