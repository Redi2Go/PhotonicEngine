package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.game.mc.world.level.IBlockState;

import java.util.Optional;

public interface IrisPack {
    /**
     * The name of the shader pack
     */
    String ph$name();

    /**
     * Return {@code true} when the shader pack natively support Photonics.
     */
    boolean ph$supportsPhotonics();

    int ph$getBlockId(IBlockState block);

    static Optional<IrisPack> getCurrentPack() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
