package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.api.Disposable;
import at.redi2go.photonics.core.collect.ConcurrentLong2ObjectMap;
import at.redi2go.photonics.core.model.AbstractVoxelModel;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.block.BlockEntry;
import at.redi2go.photonics.core.rendering.world.block.BlockProvider;
import at.redi2go.photonics.core.rendering.world.block.BlockVoxel;
import at.redi2go.photonics.core.rendering.world.registry.AbstractBufferObject;
import at.redi2go.photonics.core.rendering.world.registry.AbstractManagedObject;
import at.redi2go.photonics.core.rendering.world.registry.BufferBlockRegistry;
import at.redi2go.photonics.core.rendering.world.registry.PaletteAllocation;
import at.redi2go.photonics.core.rendering.world.registry.block.variant.ContainedBlockEntry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3ic;

import java.nio.IntBuffer;

public class BufferBlockVoxel extends AbstractBufferObject implements BlockVoxel {
    private final long hashCode;

    private IntBuffer buffer = null;
    private @Nullable ConcurrentLong2ObjectMap<Variant> variants;

    public BufferBlockVoxel(
            BufferBlockRegistry registry,
            long hashCode
    ) {
        super(registry);

        this.hashCode = hashCode;
    }

    @Override
    protected long hash() {
        return hashCode;
    }

    public void allocate(int[] data) {
        initMemory(data.length << 2);
        buffer = memory.buffer().asIntBuffer();
        buffer.put(data);

        memory.upload();

        registry.scheduleOptimization(() -> {
            var wrapper = new ModelWrapper(data);
            wrapper.optimize();

            var buffer = this.buffer;
            if (buffer == null) return;

            buffer.put(0, wrapper.data);
            memory.upload();
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

    public Variant newVariant(long vertexHash, int[] tintMappings, PaletteAllocation[] palette) {
        return getVariants().computeIfAbsent(vertexHash, (hash) -> new Variant(hash, tintMappings, palette));
    }

    public IntBuffer buffer() {
        return buffer;
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
        return buffer.get(VoxelModel.toVoxelIndex(x, y, z));
    }

    @Override
    protected void dispose() {
        super.dispose();
        buffer = null;
    }

    public class Variant extends AbstractManagedObject implements BlockProvider, Disposable {
        private final long vertexHash;
        private final int[] tintMappings;
        private final PaletteAllocation[] palette;

        private boolean voxelHashReady = false;
        private long voxelHash = 0;

        public Variant(
                long vertexHash,
                int[] tintMappings,
                PaletteAllocation[] palette
        ) {
            super(BufferBlockVoxel.this.registry);

            this.vertexHash = vertexHash;
            this.tintMappings = tintMappings;
            this.palette = palette;

            isOpen = true;

            BufferBlockVoxel.this.acquire();
            for (var entry : palette)
                entry.acquire();
        }

        private long getVoxelHash() {
            if (voxelHashReady) return voxelHash;

            voxelHash = BufferBlockHeader.voxelHash(palette, BufferBlockVoxel.this);
            voxelHashReady = true;
            return voxelHash;
        }


        @Override
        protected long hash() {
            return vertexHash;
        }

        @Override
        public @Nullable BlockEntry createVariant(IntArraySet tint, int skylight, short region) {
            int[] tintArray = tint.toIntArray();

            long tintHash = 0;
            int[] paletteTint = new int[palette.length];

            for (int i = 0; i < palette.length; i++) {
                int tintValue = tintArray[tintMappings[i]];

                tintHash = tintHash * 31 + tintValue;
                paletteTint[i] = tintValue;
            }

            var header = registry.allocateBlockHeader(
                    BufferBlockVoxel.this,
                    skylight,
                    palette,
                    paletteTint,
                    getVoxelHash(),
                    tintHash
            );

            header.addVariant(this);

            return new ContainedBlockEntry(
                    region,
                    header
            );
        }

        @Override
        protected void dispose() {
            registry.freeBlockVariant(vertexHash);

            BufferBlockVoxel.this.close();
            for (var entry : palette)
                entry.close();
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
