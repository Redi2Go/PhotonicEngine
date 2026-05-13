package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.common.PhotonicsPropertiesImpl;

/**
 * Used to pass the {@link PhotonicsPropertiesImpl} to the constructor of {@link net.irisshaders.iris.shaderpack.properties.ShaderProperties}
 */
public class ShaderPropertiesBridge {
    public static PhotonicsPropertiesImpl PROPERTIES = null;

    public static PhotonicsPropertiesImpl getProperties() {
        return PROPERTIES;
    }
}
