package at.redi2go.photonics.client.config.lights.color;

import at.redi2go.photonics.client.config.Variable;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import org.joml.Vector3f;

public interface LightColor {
   Variable.Type<LightColor> TYPE = new Variable.Type<>("light_color");
   float SCALAR = 0.003921569F;

   int red();

   int blue();

   int green();

   default Vector3f toVec3f() {
      return new Vector3f(this.red() * 0.003921569F, this.blue() * 0.003921569F, this.green() * 0.003921569F);
   }

   int toOpaque();

   String toHex();

   class Adapter extends Variable.StrAdapter<LightColor, ColorVariable> {
      protected LightColor fromString(String str) throws IOException {
         if (str.startsWith("#")) {
            return RgbColor.fromHex(str);
         }

         try {
            return RgbColor.fromRgbString(str);
         } catch (CommandSyntaxException e) {
            throw new IOException(e.getMessage() + e.getContext());
         }
      }

      protected String toString(LightColor value) throws IOException {
         return value.toHex();
      }

      protected ColorVariable newVariable(String name) throws IOException {
         return new ColorVariable(name);
      }
   }
}
