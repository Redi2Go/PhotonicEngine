package at.redi2go.photonics.core.config.lights.color;

import at.redi2go.photonics.core.config.Variable;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.joml.Vector3f;

import java.io.IOException;

public interface LightColor {
    Variable.Type<LightColor> TYPE = new Variable.Type<>("light_color");

    float SCALAR = 1 / 255f;

    int red();

    int blue();

    int green();

    default Vector3f toVec3f() {
        return new Vector3f(
                red() * SCALAR,
                blue() * SCALAR,
                green() * SCALAR
        );
    }

    int toOpaque();

    String toHex();

    class Adapter extends Variable.StrAdapter<LightColor, ColorVariable> {
        @Override
        protected LightColor fromString(String str) throws IOException {
            if (str.startsWith("#"))
                return RgbColor.fromHex(str);

            try {
                return RgbColor.fromRgbString(str);
            } catch (CommandSyntaxException e) {
                throw new IOException(e.getMessage() + e.getContext());
            }
        }

        @Override
        protected String toString(LightColor value) throws IOException {
            return value.toHex();
        }

        @Override
        protected ColorVariable newVariable(String name) throws IOException {
            return new ColorVariable(name);
        }
    }
}
