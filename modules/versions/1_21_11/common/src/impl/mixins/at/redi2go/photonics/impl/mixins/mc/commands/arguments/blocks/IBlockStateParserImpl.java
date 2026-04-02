package at.redi2go.photonics.impl.mixins.mc.commands.arguments.blocks;

import at.redi2go.photonics.api.mc.commands.arguments.blocks.IBlockStateParser;
import at.redi2go.photonics.api.mc.core.IHolderLookup;
import at.redi2go.photonics.api.mc.world.level.IBlock;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("unchecked")
@Mixin(value = IBlockStateParser.class, remap = false)
public interface IBlockStateParserImpl {
    @Overwrite
    static Either<IBlockStateParser.BlockResult, IBlockStateParser.TagResult> parse(
            IHolderLookup<IBlock> holderLookup,
            StringReader stringReader,
            boolean bl
    ) throws CommandSyntaxException {
        return (Either<IBlockStateParser.BlockResult, IBlockStateParser.TagResult>) (Either<?, ?>) BlockStateParser.parseForTesting(
                (HolderLookup<Block>) holderLookup,
                stringReader,
                bl
        );
    }
}
