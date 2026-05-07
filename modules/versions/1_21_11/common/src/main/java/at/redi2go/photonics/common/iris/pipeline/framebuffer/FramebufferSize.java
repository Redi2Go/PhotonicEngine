package at.redi2go.photonics.common.iris.pipeline.framebuffer;

import net.minecraft.client.Minecraft;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public interface FramebufferSize {
    Vector2ic get();

    record Fixed(int width, int height) implements FramebufferSize {
        @Override
        public Vector2ic get() {
            return new Vector2i(width, height);
        }
    }

    record Relative(float width, float height) implements FramebufferSize {
        private static final Vector2i ONE = new Vector2i(1);

        public Relative {
            if (width < 0f || width > 1f) throw new IllegalArgumentException("width was " + width + "; expected 0..1");
            if (height < 0f || height > 1f) throw new IllegalArgumentException("height was " + width + "; expected 0..1");
        }

        @Override
        public Vector2ic get() {
            float newWidth = Minecraft.getInstance().getWindow().getWidth() * width;
            float newHeight = Minecraft.getInstance().getWindow().getHeight() * height;

            return new Vector2i(
                    newWidth,
                    newHeight,
                    RoundingMode.TRUNCATE
            ).max(ONE);
        }
    }
}
