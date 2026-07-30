package at.redi2go.photonics.client.rendering;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

class TemporalUniformStateTest {
   @Test
   void advancesExactlyOncePerFrame() {
      TemporalUniformState state = new TemporalUniformState();
      Matrix4f firstMatrix = new Matrix4f().translation(1.0F, 2.0F, 3.0F);
      Vector3d firstCamera = new Vector3d(4.0, 5.0, 6.0);
      state.update(10, firstMatrix, firstCamera);

      Matrix4f ignoredSameFrameMatrix = new Matrix4f().translation(10.0F, 20.0F, 30.0F);
      Vector3d ignoredSameFrameCamera = new Vector3d(40.0, 50.0, 60.0);
      state.update(10, ignoredSameFrameMatrix, ignoredSameFrameCamera);

      assertTrue(state.getPreviousModelViewProjection().equals(firstMatrix, 0.0F));
      assertTrue(state.getPreviousWorldCameraPosition().equals(firstCamera, 0.0));

      Matrix4f nextMatrix = new Matrix4f().translation(7.0F, 8.0F, 9.0F);
      Vector3d nextCamera = new Vector3d(10.0, 11.0, 12.0);
      state.update(11, nextMatrix, nextCamera);

      assertTrue(state.getPreviousModelViewProjection().equals(firstMatrix, 0.0F));
      assertTrue(state.getPreviousWorldCameraPosition().equals(firstCamera, 0.0));
   }
}
