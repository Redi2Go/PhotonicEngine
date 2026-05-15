package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.BlockHeaderMemory;
import at.redi2go.photonics.core.rendering.world.registry.MemoryOwner;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;

import java.util.List;

public class BlockHeader extends MemoryOwner<BlockHeader, BlockHeaderMemory> {
    private final int[] tint;
    private final List<ManagedRef<PaletteObject.Entry>> palette;
    private final ManagedRef<BlockVoxel> blockVoxel;

    private final long hashCode;

    public BlockHeader(
            WorldRegistry registry,
            int[] tint,
            List<ManagedRef<PaletteObject.Entry>> palette,
            ManagedRef<BlockVoxel> blockVoxel,
            long voxelHash,
            long tintHash
    ) {
        super(registry);

        this.tint = tint;
        this.palette = palette;
        this.blockVoxel = blockVoxel;

        this.hashCode = (voxelHash * 31) + tintHash;
    }

    public int voxelEntry() {
        return memoryOrThrow().entryData();
    }

    @Override
    protected void loadDependants(List<ManagedRef<?>> output) {
        output.addAll(palette);
        output.add(blockVoxel);
    }

    @Override
    protected BlockHeader getWrappedValue() {
        return this;
    }

    public void allocate() {
        setMemory(registry.worldAllocator().allocateBlockHeader(palette.size()));
        var memory = memoryOrThrow();

        memory.setBlockVoxel(blockVoxel.get());
        for (int i = 0; i < palette.size(); i++)
            memory.setPaletteEntry(i, tint[i], palette.get(i).get());

        memory.upload();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockHeader other && hashCode == other.hashCode;
    }

    public static long voxelHash(List<ManagedRef<PaletteObject.Entry>> palette, BlockVoxel blockVoxel) {
        long hashCode = palette.size();
        for (ManagedRef<PaletteObject.Entry> ref : palette) {
            var entry = ref.get();

            entry.awaitAllocated();
            hashCode = hashCode * 31 + entry.entryData();
        }

        blockVoxel.awaitAllocated();
        hashCode = hashCode * 31 + blockVoxel.entryData();

        return hashCode;
    }

    public static long tintHash(int[] tints) {
        long hashCode = 0;
        for (int tint : tints)
            hashCode = hashCode * 31 + tint;

        return hashCode;
    }
}
