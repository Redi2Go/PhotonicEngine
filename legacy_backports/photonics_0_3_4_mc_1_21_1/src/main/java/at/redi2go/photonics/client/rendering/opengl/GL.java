package at.redi2go.photonics.client.rendering.opengl;

public class GL {
   private static final int[] versions = new int[]{30, 11, 31};

   public static int pGetInternalFormat(String internalFormat) {
      for (int version : versions) {
         try {
            return pGetConstant(version, internalFormat);
         } catch (RuntimeException var6) {
         }
      }

      throw new RuntimeException(new NoSuchFieldException(internalFormat));
   }

   public static int pGetConstant(int version, String constantName) {
      try {
         return (Integer)Class.forName("org.lwjgl.opengl.GL" + version).getField("GL_" + constantName).get(null);
      } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }
}
