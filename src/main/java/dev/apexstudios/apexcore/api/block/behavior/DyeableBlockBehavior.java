package dev.apexstudios.apexcore.api.block.behavior;

import dev.apexstudios.apexcore.api.util.ApexUtil;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class DyeableBlockBehavior extends SimplePropertyBlockBehavior<DyeColor, EnumProperty<DyeColor>> {
    public static final BlockBehaviorType<DyeableBlockBehavior> TYPE = new BlockBehaviorType<>(DyeableBlockBehavior::new);

    private DyeableBlockBehavior(BlockBehaviorRegistration registration) {
        super(registration, EnumProperty.create("color", DyeColor.class), DyeColor.WHITE);
    }

    public DyeColor getForPlacement(BlockPlaceContext context) {
        var player = context.getPlayer();

        if(player == null) {
            return defaultValue();
        }

        var otherHand = ApexUtil.getOtherHand(context.getHand());
        var stack = player.getItemInHand(otherHand);
        var color = DyeColor.getColor(stack);

        if(color == null) {
            color = DyeColor.getColor(context.getItemInHand());
        }

        return Objects.requireNonNullElse(color, defaultValue());
    }

    @Override
    protected BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext context) {
        return set(blockState, getForPlacement(context));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var dye = DyeColor.getColor(stack);

        if(dye == null || player.isSecondaryUseActive()) {
            return super.useItemOn(stack, blockState, level, pos, player, hand, hitResult);
        }

        var current = get(blockState);

        if(current != dye) {
            level.setBlockAndUpdate(pos, set(blockState, dye));
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, blockState, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!player.isSecondaryUseActive()) {
            return super.useWithoutItem(blockState, level, pos, player, hitResult);
        }

        var current = get(blockState);

        if(current != defaultValue()) {
            level.setBlockAndUpdate(pos, set(blockState, defaultValue()));
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(blockState, level, pos, player, hitResult);
    }
}
