package at.redi2go.photonics.core.rendering.world.block.palette;

public abstract class PaletteEntry {
    protected final TextureData[] faces = new TextureData[6];

    protected long hashCode = 0;
    protected boolean hasTransparent = false;

    protected void copyFrom(PaletteEntry other) {
        System.arraycopy(other.faces, 0, faces, 0, 6);
        this.hasTransparent = other.hasTransparent;

        computeHashCode();
    }

    protected void computeHashCodeImpl() {
        for (int i = 0; i < 6; i++) {
            var face = faces[i];
            if (face == null) {
                hashCode*= 31;
                continue;
            }

            hashCode = 31 * hashCode + face.hashCode();
        }
    }

    public void computeHashCode() {
        hashCode = 0;

        computeHashCodeImpl();
    }

    public boolean hasTransparentFace() {
        return hasTransparent;
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
}
