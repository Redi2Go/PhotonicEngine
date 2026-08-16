package at.redi2go.photonics.game.blaze3d.images;

import java.nio.ByteBuffer;

public interface IPixelType {
    int ph$getByteSize();

    void writeInt(ByteBuffer buffer, int offset, int value);

    void writeFloat(ByteBuffer buffer, int offset, float value, boolean normalized);
}
