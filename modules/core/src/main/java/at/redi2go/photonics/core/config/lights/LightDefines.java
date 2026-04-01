package at.redi2go.photonics.core.config.lights;

import at.redi2go.photonics.core.config.Variable;
import at.redi2go.photonics.core.config.lights.color.LightColor;
import at.redi2go.photonics.core.config.lights.falloff.LightFalloff;
import at.redi2go.photonics.core.config.lights.intensity.LightIntensity;
import at.redi2go.photonics.core.config.lights.radius.LightRadius;

import java.util.LinkedHashMap;
import java.util.Optional;

@SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
public class LightDefines {
    public static final LightDefines EMPTY = new LightDefines(null);

    LinkedHashMap<String, LightColor> colors;
    LinkedHashMap<String, LightIntensity> intensities;
    LinkedHashMap<String, LightRadius> radii;
    LinkedHashMap<String, LightFalloff> falloffs;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LightDefines(Void unused) {
        final LinkedHashMap temp = new LinkedHashMap(0);

        colors = (LinkedHashMap<String, LightColor>) temp;
        intensities = (LinkedHashMap<String, LightIntensity>) temp;
        radii = (LinkedHashMap<String, LightRadius>) temp;
        falloffs = (LinkedHashMap<String, LightFalloff>) temp;
    }

    public LightDefines() {
        colors = new LinkedHashMap<>();
        intensities = new LinkedHashMap<>();
        radii = new LinkedHashMap<>();
        falloffs = new LinkedHashMap<>();
    }

    private void setOwner(LinkedHashMap<String, ?> map, Variable.Owner owner) {
        for (var value : map.values()) {
            if (value instanceof Variable<?> variable)
                variable.setOwner(owner);
        }
    }

    public void setOwner(Variable.Owner owner) {
        if (this == EMPTY) {
            colors.clear(); // Just in case this somehow gets mutated
            return;
        }

        setOwner(colors, owner);
        setOwner(intensities, owner);
        setOwner(radii, owner);
        setOwner(falloffs, owner);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
        if (type == LightColor.TYPE) return Optional.ofNullable((T) colors.get(name));
        if (type == LightIntensity.TYPE) return Optional.ofNullable((T) intensities.get(name));
        if (type == LightRadius.TYPE) return Optional.ofNullable((T) radii.get(name));
        if (type == LightFalloff.TYPE) return Optional.ofNullable((T) falloffs.get(name));

        return Optional.empty();
    }

    public class Owner implements Variable.Owner {
        @Override
        public int mod() {
            final var hash = System.identityHashCode(LightDefines.this);
            if (hash == -1) return System.identityHashCode(this);

            return hash;
        }

        @Override
        public <T> Optional<T> getValue(Variable.Type<T> type, String name) {
            return LightDefines.this.getValue(type, name);
        }
    }
}
