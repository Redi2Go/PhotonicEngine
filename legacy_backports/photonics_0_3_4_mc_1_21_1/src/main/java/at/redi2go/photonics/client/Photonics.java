package at.redi2go.photonics.client;

import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Photonics implements ModInitializer {
   public static final String MOD_ID = "photonics";
   public static Version VERSION;
   public static String MOD_VERSION;
   public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("photonics.json");
   public static final String DEFAULT_CONFIG_PATH = "assets/photonics/default_config.json";
   private static final Component PREFIX = Component.literal("[")
      .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))
      .append(Component.literal("photonics").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)))
      .append(Component.literal("] ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
   private static final Logger LOGGER = LoggerFactory.getLogger("Photonics");

   public void onInitialize() {
      ModContainer photonics = (ModContainer)FabricLoader.getInstance()
         .getModContainer("photonics")
         .orElseThrow(() -> new RuntimeException("Missing photonics mod container :("));
      VERSION = photonics.getMetadata().getVersion();
      MOD_VERSION = VERSION.getFriendlyString();
   }

   public static void info(String info, Object... objects) {
      LOGGER.info(info, objects);
   }

   public static void warn(String warning, Object... objects) {
      LOGGER.warn(warning, objects);
   }

   public static void error(Exception e) {
      LOGGER.error("Photonics reported an error:", e);
   }

   public static void error(String message, Throwable e) {
      LOGGER.error(message, e);
   }

   public static void sendStatusMessage(String message) {
      Minecraft.getInstance()
         .getChatListener()
         .handleSystemMessage(Component.empty().append(PREFIX).append(Component.literal(message).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))), true);
   }
}
