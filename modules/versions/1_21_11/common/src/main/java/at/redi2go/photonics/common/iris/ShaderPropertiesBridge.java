package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.impl.shaders.PhotonicsPropertiesImpl;

/**
 * Used to pass the {@link PhotonicsPropertiesImpl} to the constructor of {@link net.irisshaders.iris.shaderpack.properties.ShaderProperties}
 */
public class ShaderPropertiesBridge {
    public static PhotonicsPropertiesImpl PROPERTIES = null;

    public static PhotonicsPropertiesImpl consumeProperties() {
        var result = PROPERTIES;
        PROPERTIES = null;

        return result;
    }
}
