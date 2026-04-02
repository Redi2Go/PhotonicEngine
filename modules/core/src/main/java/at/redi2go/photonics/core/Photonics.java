package at.redi2go.photonics.core;

import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.PhConfigWatchThread;
import com.vdurmont.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Photonics {
    public static final Logger LOGGER = LoggerFactory.getLogger("Photonics");

    private static Semver version;
    private static boolean isDevEnvironment;

    public static void init(
           Semver modVersion,
           boolean isDevelopmentEnvironment
    ) {
        version = modVersion;
        isDevEnvironment = isDevelopmentEnvironment;

        PhConfig.reloadConfig();
        PhConfigWatchThread.INSTANCE.start();
    }

    public static Semver getVersion() {
        return version;
    }

    public static boolean isDevEnvironment() {
        return isDevEnvironment;
    }
}
