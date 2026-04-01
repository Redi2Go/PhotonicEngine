package at.redi2go.photonics.core.config.lights.falloff;

import at.redi2go.photonics.core.config.Variable;

public class FalloffVariable extends Variable<LightFalloff> implements LightFalloff {
    protected FalloffVariable(String name) {
        super(name, LightFalloff.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
