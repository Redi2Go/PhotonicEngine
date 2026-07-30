package at.redi2go.photonics.client.config.lights.color;

import at.redi2go.photonics.client.config.Variable;

public class ColorVariable extends Variable<LightColor> implements LightColor {
   protected ColorVariable(String name) {
      super(name, LightColor.TYPE);
   }

   @Override
   public int red() {
      return this.actual().red();
   }

   @Override
   public int blue() {
      return this.actual().blue();
   }

   @Override
   public int green() {
      return this.actual().green();
   }

   @Override
   public int toOpaque() {
      return this.actual().toOpaque();
   }

   @Override
   public String toHex() {
      return this.actual().toHex();
   }
}
