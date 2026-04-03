package at.redi2go.photonics.api.shaders;

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
}
