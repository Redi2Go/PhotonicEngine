package at.redi2go.photonics.game.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkAccess;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkSection;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3ic;

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

    @Nullable IChunkSection.ILightData ph$getSectionLightData(int x, int y, int z, ILightLayer layer);

    default @Nullable IChunkSection.ILightData ph$getSectionLightData(Vector3ic section, ILightLayer layer) {
        return ph$getSectionLightData(section.x(), section.y(), section.z(), layer);
    }
}
