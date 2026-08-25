package at.redi2go.photonics.engine.rendering.lights;

import at.redi2go.photonics.game.minecraft.world.level.IBlockState;

public interface HandheldItem {
    boolean isEnchanted();

    IBlockState getBlockState();
}
