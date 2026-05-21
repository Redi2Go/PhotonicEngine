package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.api.mc.world.level.IBlockState;

import java.util.Optional;

public interface HandheldItemSupplier {
    boolean isLeftHanded();

    Optional<HandheldItem> getMainHand();

    Optional<HandheldItem> getOffHand();
}
