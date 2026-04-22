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
    private final Map<MutablePaletteEntry, MutablePaletteEntry> interner = new HashMap<>();

    public void add(MutablePaletteEntry data) {
        data.makePaletteWhole();
        data.computeBuilderHashCode();

        interner.putIfAbsent(data, data);
    }

    public BlockPalette build() {
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
}
