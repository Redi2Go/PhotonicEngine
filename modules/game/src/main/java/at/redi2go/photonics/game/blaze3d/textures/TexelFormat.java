package at.redi2go.photonics.game.blaze3d.textures;

import static at.redi2go.photonics.game.blaze3d.textures.TexelUsage.SUPPORTS_IMAGE;
import static at.redi2go.photonics.game.blaze3d.textures.TexelUsage.SUPPORTS_WRITE;

public enum TexelFormat {
    RED(1, SUPPORTS_IMAGE | SUPPORTS_WRITE),
    RG(2, SUPPORTS_IMAGE | SUPPORTS_WRITE),
    RGB(3, 0),
    RGBA(4, SUPPORTS_IMAGE | SUPPORTS_WRITE),
    RED_INTEGER(1, SUPPORTS_IMAGE | SUPPORTS_WRITE),
    RG_INTEGER(2, SUPPORTS_IMAGE | SUPPORTS_WRITE),
    RGB_INTEGER(3, 0),
    RGBA_INTEGER(4, SUPPORTS_IMAGE | SUPPORTS_WRITE);

    private final @TexelUsage int usage;
    private final int componentCount;

    TexelFormat(int componentCount, @TexelUsage int usage) {
        this.usage = usage;
        this.componentCount = componentCount;
    }

    public @TexelUsage int usage() {
        return usage;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public boolean supportsWrite() {
        return (usage & SUPPORTS_WRITE) != 0;
    }

    public boolean supportsImage() {
        return (usage & SUPPORTS_IMAGE) != 0;
    }
}
