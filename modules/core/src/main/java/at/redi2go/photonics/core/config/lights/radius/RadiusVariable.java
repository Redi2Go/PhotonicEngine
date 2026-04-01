package at.redi2go.photonics.core.config.lights.radius;

import at.redi2go.photonics.core.config.Variable;

public class RadiusVariable extends Variable<LightRadius> implements LightRadius {
    protected RadiusVariable(String name) {
        super(name, LightRadius.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
