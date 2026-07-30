package at.redi2go.photonics.client.config.lights.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public record RgbColor(int packedColor) implements LightColor {
   public RgbColor(int r, int b, int g) {
      this(r << 16 | b << 8 | g);
   }

   @Override
   public int red() {
      return this.packedColor >>> 16 & 0xFF;
   }

   @Override
   public int blue() {
      return this.packedColor >>> 8 & 0xFF;
   }

   @Override
   public int green() {
      return this.packedColor & 0xFF;
   }

   @Override
   public int toOpaque() {
      return this.packedColor;
   }

   @Override
   public String toHex() {
      return "#" + String.format("%08x", this.packedColor).substring(2);
   }

   public static RgbColor fromHex(String hexString) {
      if (hexString.length() != 7) {
         throw new IllegalArgumentException("must be of length 7");
      }

      if (hexString.charAt(0) != '#') {
         throw new IllegalArgumentException("must start with #");
      }

      String color = hexString.substring(1);
      return new RgbColor(Integer.parseInt(color, 16));
   }

   public static RgbColor fromRgbString(String rgbString) throws CommandSyntaxException {
      StringReader reader = new StringReader(rgbString);
      reader.expect('r');
      reader.expect('g');
      reader.expect('b');
      reader.expect('(');
      float r = reader.readFloat();
      if (!(r < 0.0F) && !(r > 1.0F)) {
         reader.expect(',');
         reader.skipWhitespace();
         float g = reader.readFloat();
         if (!(g < 0.0F) && !(g > 1.0F)) {
            reader.expect(',');
            reader.skipWhitespace();
            float b = reader.readFloat();
            if (!(b < 0.0F) && !(b > 1.0F)) {
               reader.skipWhitespace();
               reader.expect(')');
               return new RgbColor((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F));
            } else {
               throw new IllegalArgumentException("Expected float between 0 and 1, found: " + b);
            }
         } else {
            throw new IllegalArgumentException("Expected float between 0 and 1, found: " + g);
         }
      } else {
         throw new IllegalArgumentException("Expected float between 0 and 1, found: " + r);
      }
   }
}
