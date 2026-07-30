package at.redi2go.photonics.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class MinecraftAccessor {
   public static Vector3d getCameraPosition() {
      Vec3 position = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
      return new Vector3d(position.x, position.y, position.z);
   }

   public static ClientLevel getLevel() {
      return Minecraft.getInstance().level;
   }
}
