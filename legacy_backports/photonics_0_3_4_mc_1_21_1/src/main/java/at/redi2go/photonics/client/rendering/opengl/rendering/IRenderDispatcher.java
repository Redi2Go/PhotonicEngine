package at.redi2go.photonics.client.rendering.opengl.rendering;

import at.redi2go.photonics.client.rendering.opengl.objects.TextureObject;
import at.redi2go.photonics.client.rendering.world.position.PChunkPos;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface IRenderDispatcher {
   TextureObject getTextureObject(String var1);

   Set<PChunkPos> getInboundChunks();

   boolean isChunkEmpty(PChunkPos var1);

   Matrix4f getModelViewProjectionMatrix(Vector3f var1);

   boolean isLeftHanded();

   boolean hasMainHandLight();

   @NonNls
   Vector4f[] getMainHandLight();

   boolean hasOffhandLight();

   @NonNls
   Vector4f[] getOffHandLight();

   void onChunkLoad();
}
