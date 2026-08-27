package at.redi2go.photonics.client;

import at.redi2go.photonics.engine.Photonics;
import com.vdurmont.semver4j.Semver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintCache;

import java.net.URISyntaxException;
import java.util.Optional;

public class PhotonicsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Optional<ModContainer> photonics = FabricLoader.getInstance().getModContainer("photonics");
        if (photonics.isEmpty()) throw new IllegalStateException("Where is photonics? :(");

        boolean isDevEnv = FabricLoader.getInstance().isDevelopmentEnvironment();

        try {
            Photonics.init(
                    new Semver(photonics.get().getMetadata().getVersion().getFriendlyString()),
                    isDevEnv,
                    photonics.get().getRootPaths().getFirst().resolve("assets")
            );
        } catch(URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
