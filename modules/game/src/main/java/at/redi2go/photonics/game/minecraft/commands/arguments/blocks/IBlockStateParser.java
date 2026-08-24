package at.redi2go.photonics.game.minecraft.commands.arguments.blocks;

import at.redi2go.photonics.game.minecraft.IProperty;
import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import at.redi2go.photonics.game.minecraft.core.IHolderSet;
import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Either;
import org.jspecify.annotations.Nullable;

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
