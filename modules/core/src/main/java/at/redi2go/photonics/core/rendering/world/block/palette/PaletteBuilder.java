package at.redi2go.photonics.core.rendering.world.block.palette;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: Rewrite this to do tinting after creating palettes
public class PaletteBuilder {
    private final Object2ObjectMap<MutablePaletteEntry, MutablePaletteEntry> intern = new Object2ObjectOpenHashMap<>();
    private MutablePaletteEntry[] sortedEntries;

    @SuppressWarnings("unchecked")
    private final @Nullable ObjectSet<MutablePaletteEntry>[] missingFaces = new ObjectSet[6];

    private void addMissingFaces(MutablePaletteEntry data) {
        for (int i = 0; i < 6; i++) {
            if (data.hasFace(i)) continue;

            var set = missingFaces[i];
            if (set == null) {
                set = new ObjectRBTreeSet<>();
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
