package at.redi2go.photonics.game.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkAccess;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkSection;
import org.joml.Vector3ic;
import org.jspecify.annotations.Nullable;

public interface ILevel extends ILevelReader {
    @Nullable IChunkAccess ph$getChunkOrNull(int x, int z);

    default @Nullable IChunkSection ph$getSection(int x, int y, int z) {
        var chunk = ph$getChunkOrNull(x, z);
        if (chunk == null) return null;

        return chunk.ph$sections()[ph$getSectionIndexFromSectionY(y)];
    }

    default @Nullable IChunkSection ph$getSection(Vector3ic section) {
        return ph$getSection(section.x(), section.y(), section.z());
    }

    IChunkSection.@Nullable ILightData ph$getSectionLightData(int x, int y, int z, ILightLayer layer);

    default IChunkSection.@Nullable ILightData ph$getSectionLightData(Vector3ic section, ILightLayer layer) {
        return ph$getSectionLightData(section.x(), section.y(), section.z(), layer);
    }
}
