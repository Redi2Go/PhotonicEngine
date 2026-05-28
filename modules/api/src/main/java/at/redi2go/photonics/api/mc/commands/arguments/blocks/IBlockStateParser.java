package at.redi2go.photonics.api.mc.commands.arguments.blocks;

import at.redi2go.photonics.api.mc.IProperty;
import at.redi2go.photonics.api.mc.core.IHolderLookup;
import at.redi2go.photonics.api.mc.core.IHolderSet;
import at.redi2go.photonics.api.mc.nbt.ICompoundTag;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import at.redi2go.photonics.api.mc.world.level.IBlockState;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IBlockStateParser {
    interface BlockResult {
        IBlockState ph$blockState();

        Map<IProperty<?>, Comparable<?>> ph$properties();

        @Nullable ICompoundTag ph$nbt();
    }

    interface TagResult {
        IHolderSet<IBlock> ph$tag();

        Map<String, String> ph$vagueProperties();

        @Nullable ICompoundTag ph$nbt();
    }

    static Either<BlockResult, TagResult> parse(
            IHolderLookup<IBlock> holderLookup,
            StringReader stringReader,
            boolean bl
    ) {
        throw new AssertionError(); // TO BE IMPLEMENTED BY MIXIN
    }
}
