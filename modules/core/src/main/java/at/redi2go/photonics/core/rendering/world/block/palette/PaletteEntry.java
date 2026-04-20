package at.redi2go.photonics.core.rendering.world.block.palette;

import org.jetbrains.annotations.NotNull;

public abstract class PaletteEntry implements Comparable<PaletteEntry> {
    protected short usages = 0;
    protected short presentFaces = 0;

    protected final TextureData[] faces = new TextureData[6];

    protected long hashCode = 0;
    protected boolean hasTransparent = false;

    protected void copyFrom(PaletteEntry other) {
        this.presentFaces = other.presentFaces;
        System.arraycopy(other.faces, 0, faces, 0, 6);
        this.hasTransparent = other.hasTransparent;

        computeHashCode();
    }

    public void computeHashCode() {
        hashCode = 0;

        for (int i = 0; i < 6; i++) {
            var face = faces[i];
            if (face == null) {
                hashCode*= 31;
                continue;
            }

            presentFaces = (short) (presentFaces | 1 << i);
            hashCode = 31 * hashCode + face.hashCode();
        }
    }

    public int usages() {
        return usages;
    }

    public boolean hasTransparentFace() {
        return hasTransparent;
    }

    public boolean hasMissingFace() {
        return presentFaces != 63;
    }

    public boolean hasFace(int face) {
        return ((presentFaces >> face) & 1) == 1;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hashCode);
    }

    @Override
    public boolean equals(Object obj) {
        // Hash collisions are okay here,
        // and comparing every voxel face is not fast
        return obj instanceof PaletteEntry other && hashCode == other.hashCode;
    }

    @Override
    public int compareTo(@NotNull PaletteEntry o) {
        return Integer.compare(usages, o.usages);
    }
}
