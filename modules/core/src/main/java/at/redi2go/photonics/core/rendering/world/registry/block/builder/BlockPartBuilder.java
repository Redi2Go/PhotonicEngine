package at.redi2go.photonics.core.rendering.world.registry.block.builder;

import at.redi2go.photonics.core.model.VoxelEntry;
import at.redi2go.photonics.core.model.VoxelModel;
import at.redi2go.photonics.core.rendering.world.RtVoxel;
import at.redi2go.photonics.core.rendering.world.bakery.VoxelConsumer;
import at.redi2go.photonics.core.rendering.world.block.TextureData;
import at.redi2go.photonics.core.rendering.world.block.palette.BlockPalette;
import at.redi2go.photonics.core.rendering.world.block.palette.MutablePaletteEntry;
import at.redi2go.photonics.core.rendering.world.block.palette.PaletteBuilder;
import at.redi2go.photonics.core.rendering.world.registry.WorldRegistry;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.joml.Vector3i;

public class BlockPartBuilder implements VoxelConsumer {
    private final MutablePaletteEntry[] data;

    private final Vector3i temp = new Vector3i();
    private final Vector3i minVoxel = new Vector3i();
    private final Vector3i maxVoxel = new Vector3i();

    public BlockPartBuilder() {
        this.data = new MutablePaletteEntry[RtVoxel.ENTRIES_SIZE];
    }

    @Override
    public void acceptVoxel(
            int x, int y, int z,
            int normal,
            int tint,
            TextureData textureData
    ) {
        if (!VoxelModel.contains(x, y, z, 16, 16, 16)) return;

        x &= 15;
        y &= 15;
        z &= 15;

        temp.set(x, y, z);

        minVoxel.min(temp);
        maxVoxel.max(temp);

        int voxelIndex = VoxelModel.toVoxelIndex(x & 15, y & 15, z & 15);
        MutablePaletteEntry entry = data[voxelIndex];

        if (entry == null) {
            entry = new MutablePaletteEntry();
            data[voxelIndex] = entry;
        }

        entry.update(normal, tint, textureData);
    }

    private BlockPalette buildPalette() {
        var builder = new PaletteBuilder();
        for (int i = 0; i < RtVoxel.ENTRIES_SIZE; i++) {
            var entry = data[i];
            if (entry == null) continue;

            builder.add(entry);
        }

        return builder.build();
    }

    public BuildResult build() {
        var palette = buildPalette();
        var voxelData = new int[RtVoxel.ENTRIES_SIZE];

        long hash = 0;
        for (int voxelIndex = 0; voxelIndex < RtVoxel.ENTRIES_SIZE; voxelIndex++) {
            var paletteEntry = data[voxelIndex];
            var voxelEntry = palette.getIndex(paletteEntry) << 1;

            hash = hash * 31 + (((long) voxelEntry << 14) | voxelIndex);

            handleEntry: {
                if (paletteEntry == null) break handleEntry;

                if (paletteEntry.hasTransparentFace())
                    voxelEntry |= 1;
            }

            voxelData[voxelIndex] = paletteEntry == null ? VoxelModel.makeAirEntry(voxelIndex) : VoxelEntry.toData(voxelEntry);
        }

        Vector3i edgeLengths = maxVoxel.sub(minVoxel);
        return new BuildResult(
                hash,
                edgeLengths.x * edgeLengths.y * edgeLengths.z,
                palette,
                voxelData
        );
    }

    public record BuildResult(
            long hash,
            int volume,
            BlockPalette palette,
            int[] voxelData
    ) {};
}
