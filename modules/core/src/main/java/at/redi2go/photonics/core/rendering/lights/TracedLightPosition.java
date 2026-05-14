package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.mc.world.level.IBlockState;
import at.redi2go.photonics.core.config.lights.BlockLightInfo;
import org.joml.RoundingMode;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Objects;

public final class TracedLightPosition {
    private final int blockId;
    private final Vector3d pos;
    private final IBlockState blockState;
    private final BlockLightInfo lightInfo;

    private int luminanceMod = 0;
    private double luminance = 0f;

    public TracedLightPosition(
            int blockId,
            Vector3d pos,
            IBlockState blockState,
            BlockLightInfo lightInfo
    ) {
        this.blockId = blockId;
        this.pos = pos;
        this.blockState = blockState;
        this.lightInfo = lightInfo;
    }

    public int blockId() {
        return blockId;
    }

    public Vector3d pos() {
        return pos;
    }

    public Vector3i blockPos() {
        return new Vector3i(pos, RoundingMode.TRUNCATE);
    }

    public IBlockState blockState() {
        return blockState;
    }

    public BlockLightInfo lightInfo() {
        return lightInfo;
    }

    public double getLuminance(Vector3d cameraPosition, int mod) {
        if (this.luminanceMod != mod) {
            luminance = lightInfo.luminanceFrom(pos, cameraPosition);
            luminanceMod = mod;
        }

        return luminance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TracedLightPosition) obj;
        return this.blockId == that.blockId &&
                Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.blockState, that.blockState) &&
                Objects.equals(this.lightInfo, that.lightInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockId, pos, blockState, lightInfo);
    }

    @Override
    public String toString() {
        return "TracedLightPosition[" +
                "blockId=" + blockId + ", " +
                "pos=" + pos + ", " +
                "blockState=" + blockState + ", " +
                "lightInfo=" + lightInfo + ']';
    }

}
