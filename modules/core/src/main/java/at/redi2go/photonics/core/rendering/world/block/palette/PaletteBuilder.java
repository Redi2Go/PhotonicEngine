package at.redi2go.photonics.core.rendering.world.block.palette;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class PaletteBuilder {
    private final Map<MutablePaletteEntry, MutablePaletteEntry> interner = new LinkedHashMap<>();

    public void add(MutablePaletteEntry data) {
        data.makePaletteWhole();
        data.computeHashCode();

        interner.computeIfAbsent(data, e -> e)
                .addTint(data.tint);
    }

    public BlockPalette build() {
        var entries = new ArrayList<MutablePaletteEntry>();
        var tints = new IntArrayList();
        var seen = new HashSet<>();

        for (var entry : interner.values()) {
            if (!seen.add(entry)) continue;

            entry.index = entries.size();
            entries.add(entry);
            tints.add(entry.tint);

            var tintDependencies = entry.tintDependencies;
            if (tintDependencies != null) {
                tintDependencies.defaultReturnValue(entry.index);

                for (var tintEntry : tintDependencies.int2IntEntrySet()) {
                    tintEntry.setValue(entries.size());
                    entries.add(entry);
                    tints.add(tintEntry.getIntKey());
                }
            }
        }

        return new BlockPalette(interner, entries, tints);
    }
}
