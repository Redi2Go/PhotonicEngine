package at.redi2go.photonics.impl.minecraft.world.level;

import at.redi2go.photonics.game.minecraft.core.IBlockPos;
import at.redi2go.photonics.game.minecraft.core.IRegistryAccess;
import at.redi2go.photonics.game.minecraft.world.level.ILevel;
import at.redi2go.photonics.game.minecraft.world.level.ILightLayer;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkAccess;
import at.redi2go.photonics.game.minecraft.world.level.chunk.IChunkSection;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Level.class)
public abstract class LevelImpl implements ILevel {
    @Shadow
    public abstract @Nullable ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl);

    @Shadow
    public abstract LevelLightEngine getLightEngine();

    @Override
    public @Nullable IChunkAccess ph$getChunkOrNull(int x, int z) {
        return (IChunkAccess) getChunk(x, z, ChunkStatus.FULL, false);
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public IChunkSection.@Nullable ILightData ph$getSectionLightData(int x, int y, int z, ILightLayer layer) {
        var layerListener = getLightEngine().getLayerListener((LightLayer) (Object) layer);
        return (IChunkSection.ILightData) layerListener.getDataLayerData(SectionPos.of(x, y, z));
    }
}
