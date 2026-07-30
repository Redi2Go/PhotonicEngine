package at.redi2go.photonics.client.rendering.opengl.rendering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class ShaderUtil {
   private static final Pattern FUNCTION_PATTERN = Pattern.compile("(\\w+\\s+\\w+\\(.*\\))\\s*\\{");
   private static final List<Pair<String, String>> IRIS_UNIFORMS = List.of(
      Pair.of("int", "heldItemId"),
      Pair.of("int", "heldBlockLightValue"),
      Pair.of("int", "heldItemId2"),
      Pair.of("int", "heldBlockLightValue2"),
      Pair.of("int", "fogMode"),
      Pair.of("float", "fogStart"),
      Pair.of("float", "fogEnd"),
      Pair.of("int", "fogShape"),
      Pair.of("float", "fogDensity"),
      Pair.of("vec3", "fogColor"),
      Pair.of("vec3", "skyColor"),
      Pair.of("int", "worldTime"),
      Pair.of("int", "worldDay"),
      Pair.of("int", "moonPhase"),
      Pair.of("int", "frameCounter"),
      Pair.of("float", "frameTime"),
      Pair.of("float", "frameTimeCounter"),
      Pair.of("float", "sunAngle"),
      Pair.of("float", "shadowAngle"),
      Pair.of("float", "rainStrength"),
      Pair.of("float", "aspectRatio"),
      Pair.of("float", "viewWidth"),
      Pair.of("float", "viewHeight"),
      Pair.of("float", "near"),
      Pair.of("float", "far"),
      Pair.of("vec3", "sunPosition"),
      Pair.of("vec3", "moonPosition"),
      Pair.of("vec3", "shadowLightPosition"),
      Pair.of("vec3", "upPosition"),
      Pair.of("vec3", "cameraPosition"),
      Pair.of("vec3", "previousCameraPosition"),
      Pair.of("mat4", "gbufferModelView"),
      Pair.of("mat4", "gbufferModelViewInverse"),
      Pair.of("mat4", "gbufferPreviousModelView"),
      Pair.of("mat4", "gbufferProjection"),
      Pair.of("mat4", "gbufferProjectionInverse"),
      Pair.of("mat4", "gbufferPreviousProjection"),
      Pair.of("mat4", "shadowProjection"),
      Pair.of("mat4", "shadowProjectionInverse"),
      Pair.of("mat4", "shadowModelView"),
      Pair.of("mat4", "shadowModelViewInverse"),
      Pair.of("float", "wetness"),
      Pair.of("float", "eyeAltitude"),
      Pair.of("ivec2", "eyeBrightness"),
      Pair.of("ivec2", "eyeBrightnessSmooth"),
      Pair.of("ivec2", "terrainTextureSize"),
      Pair.of("int", "terrainIconSize"),
      Pair.of("int", "isEyeInWater"),
      Pair.of("float", "nightVision"),
      Pair.of("float", "blindness"),
      Pair.of("float", "screenBrightness"),
      Pair.of("int", "hideGUI"),
      Pair.of("float", "centerDepthSmooth"),
      Pair.of("ivec2", "atlasSize"),
      Pair.of("vec4", "spriteBounds"),
      Pair.of("vec4", "entityColor"),
      Pair.of("int", "entityId"),
      Pair.of("int", "blockEntityId"),
      Pair.of("ivec4", "blendFunc"),
      Pair.of("int", "instanceId"),
      Pair.of("float", "playerMood"),
      Pair.of("int", "renderStage"),
      Pair.of("int", "bossBattle"),
      Pair.of("mat4", "modelViewMatrix"),
      Pair.of("mat4", "modelViewMatrixInverse"),
      Pair.of("mat4", "projectionMatrix"),
      Pair.of("mat4", "projectionMatrixInverse"),
      Pair.of("mat4", "textureMatrix"),
      Pair.of("mat3", "normalMatrix"),
      Pair.of("vec3", "modelOffset"),
      Pair.of("float", "alphaTestRef"),
      Pair.of("float", "darknessFactor"),
      Pair.of("float", "darknessLightFactor"),
      Pair.of("sampler2D", "gtexture"),
      Pair.of("sampler2D", "lightmap"),
      Pair.of("sampler2D", "normals"),
      Pair.of("sampler2D", "specular"),
      Pair.of("sampler2D", "shadow"),
      Pair.of("sampler2D", "watershadow"),
      Pair.of("sampler2D", "shadowtex0"),
      Pair.of("sampler2D", "shadowtex1"),
      Pair.of("sampler2D", "depthtex0"),
      Pair.of("sampler2D", "gaux1"),
      Pair.of("sampler2D", "gaux2"),
      Pair.of("sampler2D", "gaux3"),
      Pair.of("sampler2D", "gaux4"),
      Pair.of("sampler2D", "colortex4"),
      Pair.of("sampler2D", "colortex5"),
      Pair.of("sampler2D", "colortex6"),
      Pair.of("sampler2D", "colortex7"),
      Pair.of("sampler2D", "colortex8"),
      Pair.of("sampler2D", "colortex9"),
      Pair.of("sampler2D", "colortex10"),
      Pair.of("sampler2D", "colortex11"),
      Pair.of("sampler2D", "colortex12"),
      Pair.of("sampler2D", "colortex13"),
      Pair.of("sampler2D", "colortex14"),
      Pair.of("sampler2D", "colortex15"),
      Pair.of("sampler2D", "depthtex1"),
      Pair.of("sampler2D", "shadowcolor"),
      Pair.of("sampler2D", "shadowcolor0"),
      Pair.of("sampler2D", "shadowcolor1"),
      Pair.of("sampler2D", "noisetex")
   );

   public static Matrix4f createScreenCameraMatrix(Matrix4fc projectionMatrix, Matrix4fc rotationMatrix) {
      return new Matrix4f(projectionMatrix).mul(rotationMatrix).invert();
   }

   public static String preprocessAutoUniforms(String source) {
      int versionIndex = source.indexOf("#version");
      if (versionIndex == -1) {
         return source;
      }

      int nextLine = source.indexOf(10, versionIndex);
      if (nextLine == -1) {
         return source;
      }

      Set<String> foundUniforms = new HashSet<>();
      Pattern pattern = Pattern.compile("\\buniform\\s+(\\w+)\\s+(\\w+)\\s*;");
      Matcher matcher = pattern.matcher(source);

      while (matcher.find()) {
         String name = matcher.group(2);
         foundUniforms.add(name);
      }

      StringBuilder addedUniforms = new StringBuilder();

      for (Pair<String, String> uniform : IRIS_UNIFORMS) {
         if (!foundUniforms.contains(uniform.getRight())) {
            addedUniforms.append("uniform ").append((String)uniform.getLeft()).append(' ').append((String)uniform.getRight()).append(';').append('\n');
         }
      }

      return addedUniforms.isEmpty() ? source : source.substring(0, nextLine + 1) + "\n" + addedUniforms + source.substring(nextLine + 1);
   }

   public static String preprocessForward(String source) {
      String forward = "//#forward";
      if (!source.contains(forward)) {
         return source;
      }

      Matcher forwardMatcher = FUNCTION_PATTERN.matcher(source);
      Set<String> functionSignatures = new HashSet<>();

      while (forwardMatcher.find()) {
         functionSignatures.add(forwardMatcher.group(1) + ";");
      }

      return source.replace(forward, String.join("\n", functionSignatures));
   }
}
