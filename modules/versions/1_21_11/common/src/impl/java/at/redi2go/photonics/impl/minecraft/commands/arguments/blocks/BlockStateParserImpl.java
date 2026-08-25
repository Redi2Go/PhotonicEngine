package at.redi2go.photonics.impl.minecraft.commands.arguments.blocks;

import at.redi2go.photonics.game.minecraft.IProperty;
import at.redi2go.photonics.game.minecraft.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.game.minecraft.core.IHolderLookup;
import at.redi2go.photonics.game.minecraft.core.IHolderSet;
import at.redi2go.photonics.game.minecraft.nbt.ICompoundTag;
import at.redi2go.photonics.game.minecraft.world.level.IBlock;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(IBlockStateParser.class)
public interface BlockStateParserImpl {
    @Mixin(BlockStateParser.BlockResult.class)
    abstract class BlockResultImpl implements IBlockStateParser.BlockResult {
        @Shadow
        public abstract BlockState blockState();

        @Shadow
        public abstract Map<Property<?>, Comparable<?>> properties();

        @Shadow
        public abstract @Nullable CompoundTag nbt();

        @Override
        public IBlockState ph$blockState() {
            return (IBlockState) blockState();
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Map<IProperty<?>, Comparable<?>> ph$properties() {
            return (Map) properties();
        }

        @Override
        public @Nullable ICompoundTag ph$nbt() {
            return (ICompoundTag) (Object) nbt();
        }
    }

    @Mixin(BlockStateParser.TagResult.class)
    abstract class TagResultImpl implements IBlockStateParser.TagResult {
        @Shadow
        public abstract HolderSet<Block> tag();

        @Shadow
        public abstract Map<String, String> vagueProperties();

        @Shadow
        public abstract @Nullable CompoundTag nbt();

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public IHolderSet<IBlock> ph$tag() {
            return (IHolderSet) tag();
        }

        @Override
        public Map<String, String> ph$vagueProperties() {
            return vagueProperties();
        }

        @Override
        public @Nullable ICompoundTag ph$nbt() {
            return (ICompoundTag) (Object) nbt();
        }
    }

    @Overwrite
    @SuppressWarnings("unchecked")
    static Either<IBlockStateParser.BlockResult, IBlockStateParser.TagResult> parse(
            IHolderLookup<IBlock> holderLookup,
            StringReader stringReader,
            boolean bl
    ) throws CommandSyntaxException {
        var result = BlockStateParser.parseForTesting(
                (HolderLookup<Block>) holderLookup,
                stringReader,
                bl
        );

        return (Either<IBlockStateParser.BlockResult, IBlockStateParser.TagResult>) (Either<?, ?>) result;

    }
}
