package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.Photonics;
import at.redi2go.photonics.client.Raytracer;
import at.redi2go.photonics.client.rendering.opengl.objects.Destructable;
import net.minecraft.client.Minecraft;

public class WorldCompilerThread extends Thread implements Destructable {
   private WorldRegistry worldRegistry;
   private boolean started = false;

   public WorldCompilerThread(WorldRegistry worldRegistry) {
      this.worldRegistry = worldRegistry;
      super.setDaemon(true);
      super.setName("WorldCompilerThread");
   }

   public void ensureRunning() {
      if (!this.started) {
         this.start();
         this.started = true;
      }
   }

   public void sendStopSignal() {
      this.started = false;
   }

   @Override
   public void run() {
      super.run();

      try {
         while (this.started) {
            synchronized (Raytracer.LOCK) {
               if (this.worldRegistry == null) {
                  return;
               }

               try {
                  this.worldRegistry.compileWorld();
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }

            synchronized (this) {
               try {
                  this.wait(1000L);
               } catch (InterruptedException var5) {
               }
            }
         }
      } catch (OutOfMemoryError e) {
         Minecraft.getInstance()
            .execute(
               () -> Photonics.sendStatusMessage(
                  "The RT structure has run out of memory. Consider lowering your render distance & reloading shaders to resolve the problem."
               )
            );
      } catch (Exception | Error e) {
         Photonics.error("An error was thrown in the world compiler thread", e);
      }
   }

   @Override
   public void free() {
      this.sendStopSignal();
      this.worldRegistry = null;
   }
}
