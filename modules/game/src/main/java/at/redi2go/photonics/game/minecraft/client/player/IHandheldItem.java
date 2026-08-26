package at.redi2go.photonics.game.minecraft.client.player;

import at.redi2go.photonics.game.minecraft.world.level.IBlockState;

public interface IHandheldItem {
    boolean ph$isEnchanted();

    IBlockState ph$getBlockState();
}
