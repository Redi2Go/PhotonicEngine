package at.redi2go.photonics.core.rendering.lights;

import at.redi2go.photonics.core.rendering.world.WorldOrigin;
import it.unimi.dsi.fastutil.ints.IntIntBiConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.max;

public class LightList extends AbstractList<TracedLightPosition> {
    private final TracedLightPosition[] lights;
    private final WorldOrigin origin;

    public LightList(TracedLightPosition[] lights, WorldOrigin origin) {
        this.lights = lights;
        this.origin = origin;
    }

    public WorldOrigin origin() {
        return origin;
    }

    @Override
    public int size() {
        return lights.length;
    }

    public boolean containsIndex(int index) {
        return index < size();
    }

    @Override
    public TracedLightPosition get(int index) {
        Objects.checkIndex(index, size());
        return lights[index];
    }

    public Mapping createMapping(LightList previous) {
        var mapping = new Mapping();
        for (int i = 0; i < max(size(), previous.size()); i++) {
            if (previous.containsIndex(i))
                mapping.addBefore(previous.get(i), i);

            if (containsIndex(i))
                mapping.addAfter(get(i), i);
        }

        return mapping;
    }

    public static class Mapping {
        private final Map<Vector3i, LightInvalidation> mapping = new HashMap<>();

        private Mapping() {

        }

        private LightInvalidation createInvalidation(Vector3i blockPos) {
            return mapping.computeIfAbsent(blockPos, LightInvalidation::new);
        }

        private void addBefore(TracedLightPosition light, int index) {
            var invalidation = createInvalidation(light.blockPos());
            invalidation.before = light.lightInfo();
            invalidation.beforeIndex = index;
        }

        private void addAfter(TracedLightPosition light, int index) {
            var invalidation = createInvalidation(light.blockPos());
            invalidation.after = light.lightInfo();
            invalidation.afterIndex = index;
        }

        public void forEachIndex(IntIntBiConsumer consumer) {
            for (var e : mapping.entrySet()) {
                final var difference = e.getValue();

                final var before = difference.before;
                final var beforeIndex = difference.beforeIndex;

                final var after = difference.after;
                final var afterIndex = difference.afterIndex;

                if (Objects.equals(before, after)) {
                    consumer.accept(beforeIndex, afterIndex);
                    continue;
                } else if (beforeIndex != -1) {
                    consumer.accept(beforeIndex, -1);
                }
            }
        }
    }
}
