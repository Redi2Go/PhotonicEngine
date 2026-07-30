package at.redi2go.photonics.client.config.lights;

public interface LightsProvider {
   void registerLights(LightList var1);

   void registerChangeListener(Runnable var1);

   void clearListeners();
}
