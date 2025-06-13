package dev.apexstudios.apexcore.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public interface Dyeable {
    Property<DyeColor> PROPERTY = EnumProperty.create("color", DyeColor.class);
    DyeColor DEFAULT_COLOR = DyeColor.WHITE;

    default DyeColor getDyedColor(BlockState blockState) {
        return blockState.getValueOrElse(PROPERTY, DEFAULT_COLOR);
    }

    default void setDyedColor(Level level, BlockPos pos, BlockState blockState, DyeColor color) {
        level.setBlockAndUpdate(pos, blockState.trySetValue(PROPERTY, color));
    }

    static DyeColor getColor(BlockState blockState) {
        if(blockState.getBlock() instanceof Dyeable dyeable)
            return dyeable.getDyedColor(blockState);

        return blockState.getValueOrElse(PROPERTY, DEFAULT_COLOR);
    }

    static DyeColor getColor(ItemStack stack) {
        return stack.getOrDefault(DataComponents.BASE_COLOR, DEFAULT_COLOR);
    }

    static void setColor(Level level, BlockPos pos, BlockState blockState, DyeColor color) {
        if(blockState.getBlock() instanceof Dyeable dyeable)
            dyeable.setDyedColor(level, pos, blockState, color);
        else
            level.setBlockAndUpdate(pos, blockState.trySetValue(PROPERTY, color));
    }

    static void setColor(ItemStack stack, DyeColor color) {
        if(color == DEFAULT_COLOR)
            stack.remove(DataComponents.BASE_COLOR);
        else if(color != getColor(stack))
            stack.set(DataComponents.BASE_COLOR, color);
    }

    static DyeColor getColorForPlacement(BlockPlaceContext context) {
        var player = context.getPlayer();

        if(player == null)
            return DEFAULT_COLOR;

        var hand = context.getHand();
        var otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(otherHand);
        var color = DyeColor.getColor(stack);

        if(color == null)
            color = getColor(context.getItemInHand());

        return color;
    }

    static InteractionResult useItemOn(Level level, BlockPos pos, BlockState blockState, ItemStack stack) {
        var color = DyeColor.getColor(stack);
        var current = getColor(blockState);

        if(color == null || current == color)
            return InteractionResult.TRY_WITH_EMPTY_HAND;

        if(!level.isClientSide)
            setColor(level, pos, blockState, color);

        return InteractionResult.SUCCESS;
    }

    static ItemStack getCloneStack(ItemLike item, BlockState blockState, @Nullable Player player, boolean includeData) {
        var stack = new ItemStack(item);

        if(includeData || (player != null && player.isCreative())) {
            var color = getColor(blockState);
            setColor(stack, color);
        }

        return stack;
    }
}
