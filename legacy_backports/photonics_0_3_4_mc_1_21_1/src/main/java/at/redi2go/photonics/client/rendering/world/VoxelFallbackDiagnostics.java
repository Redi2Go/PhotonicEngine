package at.redi2go.photonics.client.rendering.world;

import java.util.concurrent.atomic.AtomicInteger;

public final class VoxelFallbackDiagnostics {
   private static final AtomicInteger FALLBACK_COUNT = new AtomicInteger();

   private VoxelFallbackDiagnostics() {
   }

   public static void reset() {
      FALLBACK_COUNT.set(0);
   }

   public static int recordFallback() {
      return FALLBACK_COUNT.incrementAndGet();
   }

   public static int getFallbackCount() {
      return FALLBACK_COUNT.get();
   }
}
