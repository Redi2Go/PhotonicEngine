package at.redi2go.photonics.client.rendering.world;

import at.redi2go.photonics.client.config.lights.BlockLightInfo;

public record TracedLightPosition(int blockId, BlockLightInfo lightInfo) {
}
