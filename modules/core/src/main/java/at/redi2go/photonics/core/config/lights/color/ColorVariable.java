package at.redi2go.photonics.core.config.lights.color;

import at.redi2go.photonics.core.config.Variable;

public class ColorVariable extends Variable<LightColor> implements LightColor {
    protected ColorVariable(String name) {
        super(name, LightColor.TYPE);
    }

    @Override
    public int red() {
        return actual().red();
    }

    @Override
    public int blue() {
        return actual().blue();
    }

    @Override
    public int green() {
        return actual().green();
    }

    @Override
    public int toOpaque() {
        return actual().toOpaque();
    }

    @Override
    public String toHex() {
        return actual().toHex();
    }
}
