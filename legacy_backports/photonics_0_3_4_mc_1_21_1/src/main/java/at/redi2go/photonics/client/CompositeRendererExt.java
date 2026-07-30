package at.redi2go.photonics.client;

import at.redi2go.photonics.client.rendering.opengl.rendering.PhotonicsShader;
import java.util.List;

public interface CompositeRendererExt {
   List<PhotonicsShader> photonics$getPhotonicsShaders();

   void photonics$setPhotonicsShaders(List<PhotonicsShader> var1);
}
