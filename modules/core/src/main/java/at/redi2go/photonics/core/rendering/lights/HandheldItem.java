package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.mc.world.level.IBlockState;

public interface HandheldItem {
    boolean isEnchanted();

    IBlockState getBlockState();
}
