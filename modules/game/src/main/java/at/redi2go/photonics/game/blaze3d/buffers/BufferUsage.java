package at.redi2go.photonics.game.blaze3d.buffers;

public @interface BufferUsage {
    int MAP_READ = 1;
    int MAP_WRITE = 2;
    int HINT_CLIENT_STORAGE = 4;
    int COPY_DST = 8;
    int COPY_SRC = 16;
    int VERTEX = 32;
    int INDEX = 64;
    int UNIFORM = 128;
    int UNIFORM_TEXEL_BUFFER = 256;
}
