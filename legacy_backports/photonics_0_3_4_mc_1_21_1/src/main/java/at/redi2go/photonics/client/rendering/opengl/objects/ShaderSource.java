package at.redi2go.photonics.client.rendering.opengl.objects;

import java.io.Serializable;
import java.util.List;

public class ShaderSource implements Serializable {
   private static final long serialVersionUID = 4110988902804301744L;
   private final String result;
   private String error;
   private final List<String> uniforms;

   public ShaderSource(String result, List<String> uniforms) {
      this.result = result;
      this.uniforms = uniforms;
   }

   public String getResult() {
      return this.result;
   }

   public List<String> getUniforms() {
      return this.uniforms;
   }

   public String getError() {
      return this.error;
   }

   public void setError(String error) {
      this.error = error;
   }
}
