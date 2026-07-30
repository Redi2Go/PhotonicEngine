package at.redi2go.photonics.client.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3d;

public final class TemporalUniformState {
   public static final TemporalUniformState INSTANCE = new TemporalUniformState();
   private final Matrix4f currentModelViewProjection = new Matrix4f();
   private final Matrix4f previousModelViewProjection = new Matrix4f();
   private final Vector3d currentWorldCameraPosition = new Vector3d();
   private final Vector3d previousWorldCameraPosition = new Vector3d();
   private int frame = Integer.MIN_VALUE;

   public synchronized void reset() {
      this.frame = Integer.MIN_VALUE;
      this.currentModelViewProjection.identity();
      this.previousModelViewProjection.identity();
      this.currentWorldCameraPosition.zero();
      this.previousWorldCameraPosition.zero();
   }

   public synchronized void update(int frame, Matrix4f modelViewProjection, Vector3d worldCameraPosition) {
      if (this.frame == frame) {
         return;
      }

      if (this.frame == Integer.MIN_VALUE) {
         this.previousModelViewProjection.set(modelViewProjection);
         this.previousWorldCameraPosition.set(worldCameraPosition);
      } else {
         this.previousModelViewProjection.set(this.currentModelViewProjection);
         this.previousWorldCameraPosition.set(this.currentWorldCameraPosition);
      }

      this.currentModelViewProjection.set(modelViewProjection);
      this.currentWorldCameraPosition.set(worldCameraPosition);
      this.frame = frame;
   }

   public synchronized Matrix4f getPreviousModelViewProjection() {
      return new Matrix4f(this.previousModelViewProjection);
   }

   public synchronized Vector3d getPreviousWorldCameraPosition() {
      return new Vector3d(this.previousWorldCameraPosition);
   }
}
