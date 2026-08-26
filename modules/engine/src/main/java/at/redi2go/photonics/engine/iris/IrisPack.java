package at.redi2go.photonics.engine.iris;

import at.redi2go.photonics.game.minecraft.world.level.IBlockState;

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

    String readFile(IrisPackPath path);

    static Optional<IrisPack> getCurrentPack() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
