package at.redi2go.photonics.engine.rendering.lights;

import java.util.Optional;

public interface HandheldItemSupplier {
    boolean isLeftHanded();

    Optional<HandheldItem> getMainHand();

    Optional<HandheldItem> getOffHand();
}
