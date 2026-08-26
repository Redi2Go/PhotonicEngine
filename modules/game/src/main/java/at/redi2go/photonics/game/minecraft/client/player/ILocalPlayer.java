package at.redi2go.photonics.game.minecraft.client.player;

import java.util.Optional;

public interface ILocalPlayer {
    boolean ph$isLeftHanded();

    Optional<IHandheldItem> ph$getMainHand();

    Optional<IHandheldItem> ph$getOffHand();

}
