package at.redi2go.photonics.core.config.lights.intensity;

import at.redi2go.photonics.core.config.Variable;

public class IntensityVariable extends Variable<LightIntensity> implements LightIntensity {
    protected IntensityVariable(String name) {
        super(name, LightIntensity.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
