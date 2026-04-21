package at.redi2go.photonics.core;

import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.PhConfigWatchThread;
import com.vdurmont.semver4j.Semver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class Photonics {
    public static final Logger LOGGER = LoggerFactory.getLogger("Photonics");

    private static Semver version;
    private static boolean isDevEnvironment;
    private static Path assets;

    public static void init(
            Semver modVersion,
            boolean isDevelopmentEnvironment,
            Path assetsPath
    ) throws URISyntaxException {
        version = modVersion;
        isDevEnvironment = isDevelopmentEnvironment;
        assets = assetsPath;

        PhConfig.reloadConfig();
        PhConfigWatchThread.INSTANCE.start();
    }

    public static Semver getVersion() {
        return version;
    }

    public static boolean isDevEnvironment() {
        return isDevEnvironment;
    }

    public static Path getAssetsPath() {
        return assets;
    }
}
