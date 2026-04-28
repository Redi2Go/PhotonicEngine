package at.redi2go.photonics.core;

import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.PhConfigWatchThread;
import com.vdurmont.semver4j.Semver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Optional;

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

    public static String getVersionString() {
        final String major = version.getMajor() != null ? version.getMajor().toString() : "0";
        final String minor = version.getMinor() != null ? version.getMinor().toString() : "00";
        final String patch = version.getPatch() != null ? version.getPatch().toString() : "00";

        return StringUtils.stripStart(
                major +
                        StringUtils.leftPad(minor, 2, '0') +
                        StringUtils.leftPad(patch, 2, '0'),
                "0"
        );
    }

    public static boolean isDevEnvironment() {
        return isDevEnvironment;
    }

    public static Path getAssetsPath() {
        return assets;
    }
}
