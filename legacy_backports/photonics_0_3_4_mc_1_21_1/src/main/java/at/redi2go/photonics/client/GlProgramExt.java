package at.redi2go.photonics.client;

import at.redi2go.photonics.client.rendering.opengl.rendering.PhotonicsShader;

public interface GlProgramExt {
   PhotonicsShader photonics$getPhotonicsShader();

   void photonics$setPhotonicsShader(PhotonicsShader var1);
}
