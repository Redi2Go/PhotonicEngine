package at.redi2go.photonics.core.rendering.world.block.palette;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaletteBuilder {
    private static final Object NO_FACE_DATA = new Object();

    private final Map<MutablePaletteEntry, MutablePaletteEntry> interner = new HashMap<>();
    private final Map<Object, List<MutablePaletteEntry>>[] faces = new Map[6];

    public void add(MutablePaletteEntry data) {
        data.computeBuilderHashCode();

        var result = interner.computeIfAbsent(data, (e) -> e);
        if (result == data) insertFaceData(data);
    }

    /**
     * Combines different palette entries to reduce the number of used palettes.
     * This needs to work with sorted values so that tinted blocks are merged the same way
     */
    private void merge() {
        var mergeQueue = new ArrayDeque<>(interner.values());

        while (!mergeQueue.isEmpty()) {
            outerLoop:
            {
                var entry = mergeQueue.pop();

                for (int f = 0; f < 6; f++) {
                    var key = key(entry, f);

                    var map = faces[f];
                    var candidates = map.get(key);

                    for (int i = 0; i < candidates.size(); i++) {
                        var candidate = candidates.get(i);
                        if (candidate.mergedEntry != null || candidate == entry) continue;
                        if (!candidate.canMerge(entry)) continue;

                        entry.mergedEntry = candidate;

                        candidate.addFaces(entry);
                        mergeQueue.addLast(candidate);

                        break outerLoop;
                    }
                }
            }
        }
    }

    public BlockPalette build() {
        merge();

        var entries = new ArrayList<MutablePaletteEntry>();
        var tints = new IntArrayList();

        int i = 0;

        for (var entry : interner.values()) {
            if (entry.mergedEntry != null) continue;

            entries.add(entry);
            entry.index = i++;
            tints.add(entry.tint);

            var tintDependencies = entry.tintDependencies;
            if (tintDependencies != null) {
                tintDependencies.defaultReturnValue(entry.index);

                for (var tintEntry : tintDependencies.int2IntEntrySet()) {
                    if (tintEntry.getIntKey() == entry.tint) continue;

                    entries.add(entry);
                    tintEntry.setValue(i++);
                    tints.add(tintEntry.getIntKey());
                }
            }
        }

        return new BlockPalette(interner, entries, tints);
    }

    private static Object key(MutablePaletteEntry entry, int face) {
        var faceData = entry.faces[face];
        return faceData == null ? NO_FACE_DATA : face;
    }

    private void insertFaceData(MutablePaletteEntry entry) {
        for (int i = 0; i < 6; i++) {
            var key = key(entry, i);

            var map = faces[i];
            if (map == null) {
                map = new HashMap<>();
                faces[i] = map;
            }

            map.computeIfAbsent(key, (k) -> new ArrayList<>())
                    .add(entry);
        }
    }
}
