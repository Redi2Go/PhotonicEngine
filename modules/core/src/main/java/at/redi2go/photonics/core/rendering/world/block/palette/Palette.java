package at.redi2go.photonics.core.rendering.world.block.palette;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public interface Palette {
    abstract class Entry implements Comparable<Entry> {
        protected short usages = 0;
        protected short presentFaces = -1;

        protected final TextureData[] faces = new TextureData[6];

        private int hashCode = 0;
        protected boolean hasTransparent = false;

        protected void computeHashCode() {
            for (int i = 0; i < 6; i++) {
                var face = faces[i];
                if (face == null) continue;

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
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            // Hash collisions are okay here,
            // and comparing every voxel face is not fast
            return obj instanceof Entry other && hashCode == other.hashCode;
        }

        @Override
        public int compareTo(@NotNull Palette.Entry o) {
            return Integer.compare(usages, o.usages);
        }
    }

    class Builder {
        private final Object2ObjectMap<MutablePaletteEntry, MutablePaletteEntry> intern = new Object2ObjectOpenHashMap<>();
        private MutablePaletteEntry[] sortedEntries;

        @SuppressWarnings("unchecked")
        private final @Nullable ObjectSet<MutablePaletteEntry>[] missingFaces = new ObjectSet[6];

        Builder() {

        }

        private void addMissingFaces(MutablePaletteEntry data) {
            for (int i = 0; i < 6; i++) {
                if (data.hasFace(i)) continue;

                var set = missingFaces[i];
                if (set == null) {
                    set = new ObjectAVLTreeSet<>();
                    missingFaces[i] = set;
                }

                set.add(data);
            }
        }

        public void add(MutablePaletteEntry data) {
            data.computeHashCode();

            intern.computeIfAbsent(data, (e) -> (MutablePaletteEntry) e)
                    .usages++;
        }

        private void sort() {
            sortedEntries = new MutablePaletteEntry[intern.size()];
            int i = 0;

            for (var MutablePaletteEntry : intern.values()) {
                sortedEntries[i++] = MutablePaletteEntry;
                addMissingFaces(MutablePaletteEntry);
            }

            Arrays.sort(sortedEntries);
        }

        /**
         * Combines different palette entries to reduce the number of used palettes.
         * This needs to work with sorted values so that tinted blocks are merged the same way
         */
        private void merge() {
            sort();

            for (var MutablePaletteEntry : sortedEntries) {
                if (!MutablePaletteEntry.hasMissingFace()) continue;

                merging:
                {
                    for (int i = 0; i < 6; i++) {
                        if (MutablePaletteEntry.hasFace(i)) continue;

                        var candidates = missingFaces[i];
                        if (candidates == null) continue;

                        for (var candidate : candidates) {
                            if (candidate.canMerge(MutablePaletteEntry)) {
                                candidate.addFaces(MutablePaletteEntry, missingFaces);

                                intern.put(MutablePaletteEntry, candidate);

                                break merging;
                            }
                        }
                    }
                }

                // None of the candidates match, remove it from all to save some time
                removeFromMissingFaces:
                {
                    for (int i = 0; i < 6; i++) {
                        if (MutablePaletteEntry.hasFace(i)) continue;

                        var candidates = missingFaces[i];
                        if (candidates == null) continue;

                        candidates.remove(MutablePaletteEntry);
                    }
                }
            }
        }

        public BlockPalette build() {
            sort();
            merge();

            var seen = new ObjectOpenHashSet<MutablePaletteEntry>();
            var counts = new ArrayList<MutablePaletteEntry>();

            for (var entry : intern.values()) {
                if (!seen.add(entry)) continue;

                counts.add(entry);
            }

            counts.sort(MutablePaletteEntry::compareTo);
            for (int i = 0; i < counts.size(); i++) {
                counts.get(i).index = i;
            }

            return new BlockPalette(intern, counts);
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
