package at.redi2go.photonics.core.rendering.world.block.palette;

import at.redi2go.photonics.core.Photonics;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

// TODO: Rewrite this to do tinting after creating palettes
public class PaletteBuilder {
    private static final Object NO_FACE_DATA = new Object();

    private final Object2ObjectMap<MutablePaletteEntry, MutablePaletteEntry> interner = new Object2ObjectOpenHashMap<>();
    private ArrayDeque<MutablePaletteEntry> mergeQueue;

    private final Object2ObjectMap<Object, ObjectSet<MutablePaletteEntry>>[] faces = new Object2ObjectMap[6];

    public void add(MutablePaletteEntry data) {
        data.computeHashCode();

        interner.computeIfAbsent(data, (e) -> (MutablePaletteEntry) e)
                .usages++;
    }

    private void sort() {
        MutablePaletteEntry[] sortedEntries = new MutablePaletteEntry[interner.size()];

        int i = 0;

        for (var entry : interner.values()) {
            sortedEntries[i++] = entry;
            insertFaceData(entry);
        }

        Arrays.sort(sortedEntries);

        mergeQueue = new ArrayDeque<>(sortedEntries.length);

        for (var entry : sortedEntries)
            mergeQueue.addLast(entry);
    }

    /**
     * Combines different palette entries to reduce the number of used palettes.
     * This needs to work with sorted values so that tinted blocks are merged the same way
     */
    private void merge() {
        while (!mergeQueue.isEmpty()) {
            outerLoop:
            {
                var entry = mergeQueue.pop();

                for (int i = 0; i < 6; i++) {
                    var key = key(entry, i);

                    var map = faces[i];
                    var candidates = map.get(key);

                    for (var candidate : candidates) {
                        if (candidate == entry) continue;
                        if (!candidate.canMerge(entry)) continue;

                        removeFaceData(candidate);
                        removeFaceData(entry);

                        candidate.addFaces(entry, interner);
                        insertFaceData(candidate);

                        mergeQueue.addLast(candidate);
                        break outerLoop;
                    }
                }
            }
        }
    }

    public BlockPalette build() {
        sort();
        merge();

        var seen = new ObjectOpenHashSet<MutablePaletteEntry>();
        var counts = new ArrayList<MutablePaletteEntry>();

        for (var entry : interner.values()) {
            if (!seen.add(entry)) continue;

            counts.add(entry);
            entry.dependants = null;
        }

        counts.sort(MutablePaletteEntry::compareTo);
        for (int i = 0; i < counts.size(); i++) {
            counts.get(i).index = i;
        }

        return new BlockPalette(interner, counts);
    }

    // Merge map

    private static Object key(MutablePaletteEntry entry, int face) {
        var faceData = entry.faces[face];
        return faceData == null ? NO_FACE_DATA : face;
    }

    private void insertFaceData(MutablePaletteEntry entry) {
        for (int i = 0; i < 6; i++) {
            var key = key(entry, i);

            var map = faces[i];
            if (map == null) {
                map = new Object2ObjectOpenHashMap<>();
                faces[i] = map;
            }

            map.computeIfAbsent(key, (k) -> new ObjectRBTreeSet<>())
                    .add(entry);
        }
    }

    private void removeFaceData(MutablePaletteEntry entry) {
        for (int i = 0; i < 6; i++) {
            var key = key(entry, i);
            faces[i].get(key).remove(entry);
        }
    }
}
