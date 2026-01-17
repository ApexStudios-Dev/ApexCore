package dev.apexstudios.apexcore.api.block.behavior;

import dev.apexstudios.apexcore.api.util.ApexUtil;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class DyeableBlockBehavior extends BlockBehavior {
    public static final BlockBehaviorType<DyeableBlockBehavior, Void> TYPE = BlockBehaviorType.create(DyeableBlockBehavior::new);
    public static final EnumProperty<DyeColor> PROPERTY = EnumProperty.create("color", DyeColor.class);

    private DyeableBlockBehavior(BlockBehaviorRegistration registration) {
        super(registration);

        registration.property(PROPERTY, DyeColor.WHITE);
    }

    public DyeColor get(BlockState blockState) {
        return blockState.getValueOrElse(PROPERTY, DyeColor.WHITE);
    }

    public BlockState set(BlockState blockState, DyeColor color) {
        return blockState.trySetValue(PROPERTY, color);
    }

    public DyeColor getForPlacement(BlockPlaceContext context) {
        var player = context.getPlayer();

        if(player == null) {
            return DyeColor.WHITE;
        }

        var otherHand = ApexUtil.getOtherHand(context.getHand());
        var stack = player.getItemInHand(otherHand);
        var color = DyeColor.getColor(stack);

        if(color == null) {
            color = DyeColor.getColor(context.getItemInHand());
        }

        return Objects.requireNonNullElse(color, DyeColor.WHITE);
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

        if(current != DyeColor.WHITE) {
            level.setBlockAndUpdate(pos, set(blockState, DyeColor.WHITE));
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(blockState, level, pos, player, hitResult);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        if(neighbourBlockState.is(blockState.getBlock())) {
            return set(blockState, get(neighbourBlockState));
        }

        return super.updateShape(blockState, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourBlockState, random);
    }
}
