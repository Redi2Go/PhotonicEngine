package at.redi2go.photonics.impl.minecraft.client.player;

import at.redi2go.photonics.game.minecraft.client.player.IHandheldItem;
import at.redi2go.photonics.game.minecraft.client.player.ILocalPlayer;
import at.redi2go.photonics.game.minecraft.world.level.IBlockState;
import at.redi2go.photonics.mixins.Client;
import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.Optional;

@Client
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerImpl extends AbstractClientPlayer implements ILocalPlayer {
    @Unique private static final Map<Item, BlockState> ITEM_TO_BLOCK = ImmutableMap.<Item, BlockState>builder()
            .put(Items.LAVA_BUCKET, Blocks.LAVA.defaultBlockState())
            .build();

    @Override
    public boolean ph$isLeftHanded() {
        return Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT;
    }

    @Unique
    private Optional<IHandheldItem> getHandheldItem(EquipmentSlot slot) {
        return Optional.of(getInventory())
                .map(e -> (InventoryAccessor) e)
                .map(e -> e.getEquipment().get(slot))
                .filter(stack -> !stack.isEmpty())
                .flatMap(stack -> {
                    var mapped = ITEM_TO_BLOCK.get(stack.getItem());
                    if (mapped != null) return Optional.of(new BlockItem(mapped, false));

                    return BuiltInRegistries.ITEM.getResourceKey(stack.getItem())
                            .flatMap(e -> BuiltInRegistries.BLOCK.get(e.identifier()))
                            .map(Holder.Reference::value)
                            .map(block -> new BlockItem(block.defaultBlockState(), stack.isEnchanted()));
                });
    }


    @Override
    public Optional<IHandheldItem> ph$getMainHand() {
        return getHandheldItem(EquipmentSlot.MAINHAND);
    }

    @Override
    public Optional<IHandheldItem> ph$getOffHand() {
        return getHandheldItem(EquipmentSlot.OFFHAND);
    }

    public record BlockItem(BlockState blockState, boolean isEnchanted) implements IHandheldItem {
        @Override
        public boolean ph$isEnchanted() {
            return isEnchanted;
        }

        @Override
        public IBlockState ph$getBlockState() {
            return (IBlockState) blockState;
        }
    }

    private LocalPlayerImpl(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }
}
