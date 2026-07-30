package at.redi2go.photonics.client;

import at.redi2go.photonics.client.config.PhotonicsConfig;
import at.redi2go.photonics.client.config.PhotonicsConfigWatchThread;
import at.redi2go.photonics.client.mixin.DebugScreenOverlayAccessor;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.debugchart.LocalSampleLogger;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class PhotonicsClient implements ClientModInitializer {
   public void onInitializeClient() {
      PhotonicsConfig.reloadConfig();
      PhotonicsConfigWatchThread.INSTANCE.start();

   }
}
