package at.redi2go.photonics.client.api;

import at.redi2go.photonics.client.rendering.opengl.rendering.renderers.BasicRenderer;
import at.redi2go.photonics.client.rendering.opengl.rendering.renderers.DisabledRenderer;
import at.redi2go.photonics.client.rendering.opengl.rendering.renderers.MainRenderer;
import at.redi2go.photonics.client.rendering.opengl.rendering.renderers.RestirRenderer;
import at.redi2go.photonics.client.rendering.world.WorldRegistry;
import org.apache.commons.lang3.function.TriFunction;

public enum LightingMode {
   OFF(DisabledRenderer::new),
   BASIC(BasicRenderer::new),
   RESTIR(RestirRenderer::new);

   private final TriFunction<WorldRegistry, Float, PhotonicsProperties, MainRenderer> rendererFactory;

   LightingMode(TriFunction<WorldRegistry, Float, PhotonicsProperties, MainRenderer> factory) {
      this.rendererFactory = factory;
   }

   public MainRenderer createMainRenderer(WorldRegistry worldRegistry, float renderScale, PhotonicsProperties properties) {
      return (MainRenderer)this.rendererFactory.apply(worldRegistry, renderScale, properties);
   }
}
