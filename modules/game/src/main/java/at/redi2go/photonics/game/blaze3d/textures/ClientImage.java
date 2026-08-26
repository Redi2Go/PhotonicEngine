package at.redi2go.photonics.game.blaze3d.textures;

import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.joml.Vector4ic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ClientImage {
    private final TextureFormat format;

    private final int width;
    private final int height;
    private final int depth;

    private final ByteBuffer storage;

    public ClientImage(TextureFormat format, int width, int height, int depth) {
        if (!format.supportsWrite())
            throw new IllegalArgumentException(format.name() + " is not writable");

        this.format = format;

        this.width = width;
        this.height = height;
        this.depth = depth;

        this.storage = ByteBuffer.allocateDirect(width * height * depth * format.getTexelByteSize())
                .order(ByteOrder.nativeOrder());
    }

    public ClientImage(TextureFormat format, Vector3ic size) {
        this(format, size.x(), size.y(), size.z());
    }

    public ClientImage(TextureFormat format, int width, int height) {
        this(format, width, height, 1);
    }

    public ClientImage(TextureFormat format, Vector2ic size) {
        this(format, size.x(), size.y(), 1);
    }

    public ClientImage(TextureFormat format, int width) {
        this(format, width, 1, 1);
    }

    public TextureFormat getFormat() {
        return format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public Vector3ic getSize() {
        return new Vector3i(width, height, depth);
    }

    public ByteBuffer getStorage() {
        return storage;
    }

    private void checkInBounds(int x, int y, int z) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth)
            return;

        throw new IndexOutOfBoundsException(
                String.format(
                        "[%s, %s, %s] out of bounds of image size [%s, %s, %s]",
                        x,
                        y,
                        z,
                        width,
                        height,
                        depth
                )
        );
    }

    private int getOffset(int x, int y, int z) {
        checkInBounds(x, y, z);

        int index = x + (width * y) + ((width * height) * z);
        return index * format.getTexelByteSize();
    }

    private void writeInt(int offset, int r, int g, int b, int a) {
        int componentCount = format.getTexelFormat().getComponentCount();
        TexelType texelType = format.getTexelType();

        texelType.writeInt(storage, offset, r);
        if (componentCount < 2) return;

        offset += texelType.getByteSize();
        texelType.writeInt(storage, offset, g);
        if (componentCount < 3) return;

        offset += texelType.getByteSize();
        texelType.writeInt(storage, offset, b);

        offset += texelType.getByteSize();
        texelType.writeInt(storage, offset, a);
    }

    private void writeFloat(int offset, float r, float g, float b, float a) {
        int componentCount = format.getTexelFormat().getComponentCount();
        TexelType texelType = format.getTexelType();

        texelType.writeFloat(storage, offset, r);
        if (componentCount < 2) return;

        offset += texelType.getByteSize();
        texelType.writeFloat(storage, offset, g);
        if (componentCount < 3) return;

        offset += texelType.getByteSize();
        texelType.writeFloat(storage, offset, b);

        offset += texelType.getByteSize();
        texelType.writeFloat(storage, offset, a);
    }

    public void setPixelInt(int index, int r) {
        writeInt(index * format.getTexelByteSize(), r, 0, 0, 0);
    }

    public void setPixelInt(int index, int r, int g) {
        writeInt(index * format.getTexelByteSize(), r, g, 0, 0);
    }

    public void setPixelInt(int index, Vector2ic color) {
        writeInt(index * format.getTexelByteSize(), color.x(), color.y(), 0, 0);
    }

    public void setPixelInt(int index, int r, int g, int b, int a) {
        writeInt(index * format.getTexelByteSize(), r, g, b, a);
    }

    public void setPixelInt(int index, Vector4ic color) {
        writeInt(index * format.getTexelByteSize(), color.x(), color.y(), color.z(), color.w());
    }

    public void setPixelFloat(int index, float r) {
        writeFloat(index * format.getTexelByteSize(), r, 0, 0, 0);
    }

    public void setPixelFloat(int index, float r, float g) {
        writeFloat(index * format.getTexelByteSize(), r, g, 0, 0);
    }

    public void setPixelFloat(int index, Vector2fc color) {
        writeFloat(index * format.getTexelByteSize(), color.x(), color.y(), 0, 0);
    }

    public void setPixelFloat(int index, float r, float g, float b, float a) {
        writeFloat(index * format.getTexelByteSize(), r, g, b, a);
    }

    public void setPixelFloat(int index, Vector4fc color) {
        writeFloat(index * format.getTexelByteSize(), color.x(), color.y(), color.z(), color.w());
    }

    public void setPixelInt(Vector3ic pos, int r) {
        writeInt(getOffset(pos.x(), pos.y(), pos.z()), r, 0, 0, 0);
    }

    public void setPixelInt(Vector3ic pos, int r, int g) {
        writeInt(getOffset(pos.x(), pos.y(), pos.z()), r, g, 0, 0);
    }

    public void setPixelInt(Vector3ic pos, Vector2ic color) {
        writeInt(getOffset(pos.x(), pos.y(), pos.z()), color.x(), color.y(), 0, 0);
    }

    public void setPixelInt(Vector3ic pos, int r, int g, int b, int a) {
        writeInt(getOffset(pos.x(), pos.y(), pos.z()), r, g, b, a);
    }

    public void setPixelInt(Vector3ic pos, Vector4ic color) {
        writeInt(getOffset(pos.x(), pos.y(), pos.z()), color.x(), color.y(), color.z(), color.w());
    }

    public void setPixelFloat(Vector3ic pos, float r) {
        writeFloat(getOffset(pos.x(), pos.y(), pos.z()), r, 0, 0, 0);
    }

    public void setPixelFloat(Vector3ic pos, float r, float g) {
        writeFloat(getOffset(pos.x(), pos.y(), pos.z()), r, g, 0, 0);
    }

    public void setPixelFloat(Vector3ic pos, Vector2fc color) {
        writeFloat(getOffset(pos.x(), pos.y(), pos.z()), color.x(), color.y(), 0, 0);
    }

    public void setPixelFloat(Vector3ic pos, float r, float g, float b, float a) {
        writeFloat(getOffset(pos.x(), pos.y(), pos.z()), r, g, b, a);
    }

    public void setPixelFloat(Vector3ic pos, Vector4fc color) {
        writeFloat(getOffset(pos.x(), pos.y(), pos.z()), color.x(), color.y(), color.z(), color.w());
    }

    public void setPixelInt(Vector2ic pos, int r) {
        writeInt(getOffset(pos.x(), pos.y(), 0), r, 0, 0, 0);
    }

    public void setPixelInt(Vector2ic pos, int r, int g) {
        writeInt(getOffset(pos.x(), pos.y(), 0), r, g, 0, 0);
    }

    public void setPixelInt(Vector2ic pos, Vector2ic color) {
        writeInt(getOffset(pos.x(), pos.y(), 0), color.x(), color.y(), 0, 0);
    }

    public void setPixelInt(Vector2ic pos, int r, int g, int b, int a) {
        writeInt(getOffset(pos.x(), pos.y(), 0), r, g, b, a);
    }

    public void setPixelInt(Vector2ic pos, Vector4ic color) {
        writeInt(getOffset(pos.x(), pos.y(), 0), color.x(), color.y(), color.z(), color.w());
    }

    public void setPixelFloat(Vector2ic pos, float r) {
        writeFloat(getOffset(pos.x(), pos.y(), 0), r, 0, 0, 0);
    }

    public void setPixelFloat(Vector2ic pos, float r, float g) {
        writeFloat(getOffset(pos.x(), pos.y(), 0), r, g, 0, 0);
    }

    public void setPixelFloat(Vector2ic pos, Vector2fc color) {
        writeFloat(getOffset(pos.x(), pos.y(), 0), color.x(), color.y(), 0, 0);
    }

    public void setPixelFloat(Vector2ic pos, float r, float g, float b, float a) {
        writeFloat(getOffset(pos.x(), pos.y(), 0), r, g, b, a);
    }

    public void setPixelFloat(Vector2ic pos, Vector4fc color) {
        writeFloat(getOffset(pos.x(), pos.y(), 0), color.x(), color.y(), color.z(), color.w());
    }
}
