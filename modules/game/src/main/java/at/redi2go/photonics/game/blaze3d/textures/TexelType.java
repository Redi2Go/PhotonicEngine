package at.redi2go.photonics.game.blaze3d.textures;

import java.nio.ByteBuffer;

import static at.redi2go.photonics.game.blaze3d.textures.TexelUsage.*;

public enum TexelType {
    UNORM_BYTE(1, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeUnsignedByte(buffer, index, toUnorm(value, 255.0f));
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeUnsignedByte(buffer, index, value);
        }
    },
    UNORM_SHORT(2, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeUnsignedShort(buffer, index, toUnorm(value, 65535.0f));
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeUnsignedShort(buffer, index, value);
        }
    },
    UNORM_INT(4, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeUnsignedInt(buffer, index, toUnorm(value, 4294967295.0f));
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeUnsignedInt(buffer, index, value);
        }
    },

    SNORM_BYTE(1, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeSignedByte(buffer, index, toSnorm(value, 127.0f));
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeSignedByte(buffer, index, value);
        }
    },
    SNORM_SHORT(2, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeSignedShort(buffer, index, toSnorm(value, 32767.0f));
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeSignedShort(buffer, index, value);
        }
    },
    SNORM_INT(4, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeSignedInt(buffer, index, toSnorm(value, 2147483647.0f));
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeSignedInt(buffer, index, value);
        }
    },

    HALF_FLOAT(2, SUPPORTS_IMAGE),
    FLOAT(4, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            buffer.putFloat(index, value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            buffer.putFloat(index, (float) value);
        }
    },

    BYTE(1, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeSignedByte(buffer, index, (int) value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeSignedByte(buffer, index, value);
        }
    },
    SHORT(2, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeSignedShort(buffer, index, (int) value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeSignedShort(buffer, index, value);
        }
    },
    INT(4, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeSignedInt(buffer, index, (int) value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeSignedInt(buffer, index, value);
        }
    },

    UNSIGNED_BYTE(1, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeUnsignedByte(buffer, index, (int) value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeUnsignedByte(buffer, index, value);
        }
    },
    UNSIGNED_SHORT(2, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeUnsignedShort(buffer, index, (int) value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeUnsignedShort(buffer, index, value);
        }
    },
    UNSIGNED_INT(4, SUPPORTS_IMAGE | SUPPORTS_WRITE) {
        @Override
        public void writeFloat(ByteBuffer buffer, int index, float value) {
            writeUnsignedInt(buffer, index, (int) value);
        }

        @Override
        public void writeInt(ByteBuffer buffer, int index, int value) {
            writeUnsignedInt(buffer, index, value);
        }
    },

    UNSIGNED_BYTE_3_3_2(1, 0),
    UNSIGNED_BYTE_2_3_3_REV(1, 0),
    UNSIGNED_SHORT_5_6_5(2, 0),
    UNSIGNED_SHORT_5_6_5_REV(2, 0),
    UNSIGNED_SHORT_4_4_4_4(2, 0),
    UNSIGNED_SHORT_4_4_4_4_REV(2, 0),
    UNSIGNED_SHORT_5_5_5_1(2, 0),
    UNSIGNED_SHORT_1_5_5_5_REV(2, 0),
    UNSIGNED_INT_8_8_8_8(4, 0),
    UNSIGNED_INT_8_8_8_8_REV(4, 0),
    UNSIGNED_INT_10_10_10_2(4, 0),
    UNSIGNED_INT_2_10_10_10_REV(4, SUPPORTS_IMAGE),
    UNSIGNED_INT_10F_11F_11F_REV(4, SUPPORTS_IMAGE),
    UNSIGNED_INT_5_9_9_9_REV(4, 0);

    private final @TexelUsage int usage;
    private final int byteSize;

    TexelType(int byteSize, @TexelUsage int usage) {
        this.byteSize = byteSize;
        this.usage = usage;
    }

    public @TexelUsage int usage() {
        return usage;
    }

    public int getByteSize() {
        return byteSize;
    }

    public boolean supportsWrite() {
        return (usage & SUPPORTS_WRITE) != 0;
    }

    public boolean supportsImage() {
        return (usage & SUPPORTS_IMAGE) != 0;
    }

    public void writeInt(ByteBuffer buffer, int index, int value) {
        throw new UnsupportedOperationException(name() + " does not support writes");
    }

    public void writeFloat(ByteBuffer buffer, int index, float value) {
        throw new UnsupportedOperationException(name() + " does not support writes");
    }

    private static void writeSignedByte(ByteBuffer buffer, int offset, int value) {
        buffer.put(offset, (byte) clampSignedByte(value));
    }

    private static void writeUnsignedByte(ByteBuffer buffer, int offset, int value) {
        buffer.put(offset, (byte) clampUnsignedByte(value));
    }

    private static void writeSignedShort(ByteBuffer buffer, int offset, int value) {
        buffer.putShort(offset, (short) clampSignedShort(value));
    }

    private static void writeUnsignedShort(ByteBuffer buffer, int offset, int value) {
        buffer.putShort(offset, (short) clampUnsignedShort(value));
    }

    private static void writeSignedInt(ByteBuffer buffer, int offset, int value) {
        buffer.putInt(offset, value);
    }

    private static void writeUnsignedInt(ByteBuffer buffer, int offset, int value) {
        buffer.putInt(offset, value);
    }

    private static int toUnorm(float value, float max) {
        return (int) (clampUnorm(value) * max);
    }

    private static int toSnorm(float value, float max) {
        return (int) (clampSnorm(value) * max);
    }

    private static float clampUnorm(float value) {
        return Math.min(Math.max(value, 0.0f), 1.0f);
    }

    private static float clampSnorm(float value) {
        return Math.min(Math.max(value, -1.0f), 1.0f);
    }

    private static int clampSignedByte(int value) {
        return Math.min(Math.max(value, Byte.MIN_VALUE), 0x7f);
    }

    private static int clampUnsignedByte(int value) {
        return Math.min(Math.max(value, 0), 0xff);
    }

    private static int clampSignedShort(int value) {
        return Math.min(Math.max(value, Short.MIN_VALUE), 0x7fff);
    }

    private static int clampUnsignedShort(int value) {
        return Math.min(Math.max(value, 0), 0xffff);
    }
}
