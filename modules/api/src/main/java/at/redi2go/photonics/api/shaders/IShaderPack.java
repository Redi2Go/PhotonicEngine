package at.redi2go.photonics.api.shaders;

import at.redi2go.photonics.api.mc.world.level.IBlockState;

import java.util.Optional;

public interface IShaderPack {
    /**
     * The name of the shader pack
     */
    String name();

    /**
     * Return {@code true} when the shader pack natively support Photonics.
     */
    boolean supportsPhotonics();

    PhotonicsProperties properties();

    int getBlockId(IBlockState block);

    static Optional<IShaderPack> getCurrentPack() {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
