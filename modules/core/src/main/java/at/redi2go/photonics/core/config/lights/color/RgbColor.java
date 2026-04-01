package at.redi2go.photonics.core.config.lights.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public final record RgbColor(int packedColor) implements LightColor {
    public RgbColor(int r, int b, int g) {
        this((r << 16) | (b << 8) | g);
    }

    @Override
    public int red() {
        return (packedColor >>> 16) & 0xff;
    }

    @Override
    public int blue() {
        return (packedColor >>> 8) & 0xff;
    }

    @Override
    public int green() {
        return packedColor & 0xff;
    }

    @Override
    public int toOpaque() {
        return packedColor;
    }

    @Override
    public String toHex() {
        return "#" + String.format("%08x", packedColor).substring(2);
    }

    public static RgbColor fromHex(String hexString) {
        if (hexString.length() != 7)
            throw new IllegalArgumentException("must be of length 7");

        if (hexString.charAt(0) != '#')
            throw new IllegalArgumentException("must start with #");

        final String color = hexString.substring(1);
        return new RgbColor(Integer.parseInt(color, 16));
    }

    public static RgbColor fromRgbString(String rgbString) throws CommandSyntaxException {
        StringReader reader = new StringReader(rgbString);
        reader.expect('r');
        reader.expect('g');
        reader.expect('b');
        reader.expect('(');

        float r = reader.readFloat();
        if (r < 0f || r > 1f)
            throw new IllegalArgumentException("Expected float between 0 and 1, found: " + r);

        reader.expect(',');
        reader.skipWhitespace();

        float g = reader.readFloat();
        if (g < 0f || g > 1f)
            throw new IllegalArgumentException("Expected float between 0 and 1, found: " + g);

        reader.expect(',');
        reader.skipWhitespace();

        float b = reader.readFloat();
        if (b < 0f || b > 1f)
            throw new IllegalArgumentException("Expected float between 0 and 1, found: " + b);

        reader.skipWhitespace();
        reader.expect(')');

        return new RgbColor(
                (int) (r * 255f),
                (int) (g * 255f),
                (int) (b * 255f)
        );
    }
}
