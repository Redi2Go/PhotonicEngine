package at.redi2go.photonics.engine.config.lights.falloff;

import at.redi2go.photonics.engine.config.Variable;

public class FalloffVariable extends Variable<LightFalloff> implements LightFalloff {
    protected FalloffVariable(String name) {
        super(name, LightFalloff.TYPE);
    }

    @Override
    public float get() {
        return actual().get();
    }
}
