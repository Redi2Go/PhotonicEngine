package at.redi2go.photonics.common.iris;

import at.redi2go.photonics.core.iris.patching.ShaderPatcher;

/**
 * Used to pass the {@link ShaderPatcher} to the constructor of {@link net.irisshaders.iris.shaderpack.include.IncludeGraph}
 */
public class PatcherBridge {
    public static ShaderPatcher PATCHER = null;

    public static ShaderPatcher consumePatcher() {
        var result = PATCHER;
        PATCHER = null;

        return result;
    }
}
