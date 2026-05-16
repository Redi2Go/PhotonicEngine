package at.redi2go.photonics.core.rendering.world.registry.block;

import at.redi2go.photonics.core.rendering.world.allocator.BlockHeaderMemory;
import at.redi2go.photonics.core.rendering.world.registry.PaletteObject;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import at.redi2go.photonics.core.rendering.world.registry.objects.WorldObject;

import java.util.List;

public class BlockHeader extends WorldObject<BlockHeaderMemory> {
    private final int[] tint;
    private final List<PaletteObject> palette;
    private final BlockVoxel blockVoxel;

    private final long hashCode;

    public BlockHeader(
            WorldRegistry worldRegistry,
            int[] tint,
            List<PaletteObject> weakPalette,
            BlockVoxel weakBlockVoxel,
            long voxelHash,
            long tintHash
    ) {
        super(worldRegistry);

        this.tint = tint;
        this.palette = weakPalette;
        this.blockVoxel = weakBlockVoxel;

        this.hashCode = voxelHash ^ tintHash;
    }

    public int entryData() {
        return memoryOrThrow().entryData();
    }

    @Override
    protected void loadDependants(List<WorldObject<?>> output) {
        output.addAll(palette);
        output.add(blockVoxel);
    }

    public void allocate() {
        var memory = setMemory(() -> worldRegistry.worldAllocator().allocateBlockHeader(palette.size()));

        memory.setBlockVoxel(blockVoxel);
        for (int i = 0; i < palette.size(); i++)
            memory.setPaletteEntry(i, tint[i], palette.get(i));

        memory.upload();
    }


    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockHeader other && other.hashCode == hashCode;
    }

    public static long voxelHash(List<PaletteObject> palette, BlockVoxel blockVoxel) {
        long hashCode = palette.size();
        for (var paletteObject : palette) {
            paletteObject.awaitAllocated();
            hashCode = hashCode * 31 + paletteObject.entryData();
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
