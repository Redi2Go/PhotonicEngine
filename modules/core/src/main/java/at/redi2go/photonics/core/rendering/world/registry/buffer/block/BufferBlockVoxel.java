package at.redi2go.photonics.core.rendering.world.registry.buffer.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.api.gpu.buffers.heap.MemoryView;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.block.palette.TintBuilder;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.BufferPaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.buffer.block.variant.ContainedBlockEntry;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3ic;

import java.nio.IntBuffer;
import java.util.List;

public class BufferBlockVoxel extends BufferObject<BufferBlockVoxel, MemoryView> implements BlockVoxel {
    private final long hashCode;
    private @Nullable ConcurrentLong2ObjectMap<Variant> variants;

    public BufferBlockVoxel(BufferBlockRegistry registry, long hashCode) {
        super(registry);

        this.hashCode = hashCode;
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {

    }

    @Override
    protected BufferBlockVoxel getWrappedValue() {
        return this;
    }

    @Override
    public int begin() {
        return MemoryView.intBufferBegin(memoryOrThrow());
    }

    public void allocate(int[] data) {
        setMemory(registry.allocate(data.length << 2));
        var memory = memoryOrThrow();
        final var buffer = memory.buffer().asIntBuffer();
        buffer.put(data);

        memory.upload();

        registry.scheduleOptimization(() -> {
            var wrapper = new ModelWrapper(data);
            wrapper.optimize();

            var newMemory = memoryOrNull();
            if (newMemory == null) return;

            var newBuffer = newMemory.buffer().asIntBuffer();

            newBuffer.put(0, wrapper.data);
            newMemory.upload();
        });
    }

    private @NonNls ConcurrentLong2ObjectMap<Variant> getVariants() {
        var variants = this.variants;
        if (variants != null) return variants;

        synchronized (this) {
            variants = this.variants;
            if (variants != null) return variants;

            variants = new ConcurrentLong2ObjectMap<>(3);
            this.variants = variants;
        }

        return variants;
    }

    public ManagedRef<Variant> newVariant(long vertexHash, List<ManagedRef<BufferPaletteObject.Entry>> palette, int[] tintMappings) {
        return getVariants().computeIfAbsent(
                vertexHash,
                (hash) -> new Variant(registry, hash, makeManagedRef(), palette, tintMappings)
        ).makeManagedRef();
    }

    public IntBuffer buffer() {
        return memoryOrNull().buffer().asIntBuffer();
    }

    @Override
    public Vector3ic size() {
        return RtVoxel.SIZE_3;
    }

    @Override
    public boolean contains(int x, int y, int z) {
        return VoxelModel.contains(x, y, z, RtVoxel.SIDE_LENGTH, RtVoxel.SIDE_LENGTH, RtVoxel.SIDE_LENGTH);
    }

    @Override
    public int get(int x, int y, int z) {
        return memoryOrThrow().buffer().getInt(VoxelModel.toVoxelIndex(x, y, z) << 2);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BufferBlockVoxel other && hashCode == other.hashCode;
    }

    public static class Variant extends BufferObject<Variant, Disposable> implements BlockProvider {
        private final long vertexHash;

        private final ManagedRef<BufferBlockVoxel> blockVoxel;

        private final List<ManagedRef<BufferPaletteObject.Entry>> palette;
        private final int[] tintMappings;

        private boolean voxelHashReady = false;
        private long voxelHash = 0;

        public Variant(
                BufferBlockRegistry registry,
                long vertexHash,
                ManagedRef<BufferBlockVoxel> blockVoxel,
                List<ManagedRef<BufferPaletteObject.Entry>> palette,
                int[] tintMappings
        ) {
            super(registry);

            this.vertexHash = vertexHash;
            this.blockVoxel = blockVoxel;
            this.palette = palette;
            this.tintMappings = tintMappings;

            setMemory(NO_MEMORY);
        }

        @Override
        protected void loadDependants(List<ManagedRef<?>> output) {
            output.add(blockVoxel);
            output.addAll(palette);
        }

        @Override
        protected Variant getWrappedValue() {
            return this;
        }

        public long vertexHash() {
            return vertexHash;
        }

        private long getVoxelHash() {
            if (voxelHashReady) return voxelHash;

            voxelHash = BufferBlockHeader.voxelHash(palette, blockVoxel.get());
            voxelHashReady = true;
            return voxelHash;
        }

        @Override
        public @Nullable BlockEntry createVariant(TintBuilder.Result tintInfo, int skylight, short region) {
            long variantHash = vertexHash;
            variantHash = variantHash * 31 + tintInfo.hash();
            variantHash = variantHash * 31 + skylight;

            return new ContainedBlockEntry(
                    region,
                    registry.cacheVariantHeader(
                            variantHash,
                            (k) -> {
                                int[] tintArray = tintInfo.tints().toIntArray();

                                long tintHash = 0;
                                int[] paletteTint = new int[palette.size()];

                                for (int i = 0; i < palette.size(); i++) {
                                    int tintValue = tintArray[tintMappings[i]];

                                    tintHash = tintHash * 31 + tintValue;
                                    paletteTint[i] = tintValue;
                                }

                                var result = registry.allocateBlockHeader(
                                        blockVoxel,
                                        skylight,
                                        palette,
                                        paletteTint,
                                        getVoxelHash(),
                                        tintHash
                                ).get();

                                result.addVariant(k);

                                return result;
                            }
                    ).elevate(),
                    makeRef()
            );
        }

        @Override
        protected boolean dispose() {
            var closed = super.dispose();
            if (closed) {
                blockVoxel.get().variants.remove(vertexHash);
                registry.removeBlockVariant(vertexHash);
            }

            return closed;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(vertexHash);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Variant other && vertexHash == other.vertexHash;
        }
    }

    private static class ModelWrapper extends AbstractVoxelModel {
        int[] data;

        public ModelWrapper(int[] data) {
            super(RtVoxel.SIZE_3);

            this.data = data;
        }

        @Override
        protected int get(int index) {
            return data[index];
        }

        @Override
        protected void set(int index, int value) {
            data[index] = value;
        }

        @Override
        public void optimize() {
            super.optimize();
        }
    }
}
