package at.redi2go.photonics.core.iris;

import at.redi2go.photonics.core.config.PhConfig;
import at.redi2go.photonics.core.config.Variable;
import at.redi2go.photonics.core.config.lights.LightDefines;
import at.redi2go.photonics.core.config.lights.LightGroup;
import at.redi2go.photonics.core.config.lights.LightRegistry;
import at.redi2go.photonics.core.config.lights.LightsProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;

public abstract class AbstractIrisPackLights implements LightsProvider, Variable.Owner {
    protected LightDefines defines = LightDefines.EMPTY;
    protected LinkedHashMap<String, LightGroup> lights = new LinkedHashMap<>(0);

    @Override
    public int mod() {
        return 0;
    }


    @Override
    public void registerLights(LightRegistry lights) {
        for (final var lightGroup : this.lights.values())
            lightGroup.recordLights(
                    this,
                    lights,
                    PhConfig.getTracedOverrides(),
                    1000
            );
    }

    @Override
    public void registerChangeListener(Runnable consumer) {
        // Ignored because this never changes
    }

    @Override
    public void clearListeners() {
        // Ignored because this never changes
    }

    public static <T extends AbstractIrisPackLights> T parse(String contents, Class<T> clazz) {
        var lines = contents.split("\n");
        int start = 0;

        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];
            if (!line.isEmpty() && line.charAt(0) == '{') {
                start = i;
                break;
            }
        }

        contents = String.join("\n", Arrays.copyOfRange(lines, start, lines.length));
        contents = contents.replace("`", "\"");

        return PhConfig.GSON.fromJson(contents, clazz);
    }
}
