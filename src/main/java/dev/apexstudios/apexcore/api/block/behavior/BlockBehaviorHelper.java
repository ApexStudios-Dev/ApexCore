package dev.apexstudios.apexcore.api.block.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public interface BlockBehaviorHelper {
    static BlockState updateShape(IBehaviorBlock block, BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        var result = blockState;

        // TODO: Only for multiblocks and neighbors within the same multiblock
        /*if(neighbourBlockState.is(blockState.getBlock())) {
            for(var behavior : block.getBehaviors()) {
                result = behavior.copyProperties(result, neighbourBlockState);

                if(result.isEmpty()) {
                    break;
                }
            }

            if(result.isEmpty()) {
                return result;
            }
        }*/

        block.executeIfPresent(BlockBehaviorTypes.WATERLOGGED, behavior -> {
            if(behavior.get(blockState)) {
                ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
        });

        return result;
    }

    static InteractionResult useItemOn(IBehaviorBlock block, ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        for(var behavior : block.getBehaviors()) {
            var result = behavior.useItemOn(stack, blockState, level, pos, player, hand, hitResult);

            if(result.consumesAction()) {
                return result;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    static InteractionResult useWithoutItem(IBehaviorBlock block, BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        for(var behavior : block.getBehaviors()) {
            var result = behavior.useWithoutItem(blockState, level, pos, player, hitResult);

            if(result.consumesAction()) {
                return result;
            }
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    static @Nullable BlockState getStateForPlacement(IBehaviorBlock block, BlockState blockState, BlockPlaceContext context) {
        var result = blockState;

        for(var behavior : block.getBehaviors()) {
            result = behavior.getStateForPlacement(result, context);

            if(result == null || result.isEmpty()) {
                break;
            }
        }

        return result != null && result.isEmpty() ? null : result;
    }

    static @Nullable FluidState getFluidState(IBehaviorBlock block, BlockState blockState) {
        var behavior = block.getBehavior(BlockBehaviorTypes.WATERLOGGED);
        return behavior == null ? null : behavior.getFluidState(blockState);
    }

    static boolean canPlaceLiquid(IBehaviorBlock block, @Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
        var behavior = block.getBehavior(BlockBehaviorTypes.WATERLOGGED);
        return behavior != null && behavior.canPlaceLiquid(user, level, pos, blockState, fluid);
    }

    static boolean placeLiquid(IBehaviorBlock block, LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        var behavior = block.getBehavior(BlockBehaviorTypes.WATERLOGGED);
        return behavior != null && behavior.placeLiquid(level, pos, blockState, fluidState);
    }

    static ItemStack pickupBlock(IBehaviorBlock block, @Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState blockState) {
        var behavior = block.getBehavior(BlockBehaviorTypes.WATERLOGGED);
        return behavior == null ? ItemStack.EMPTY : behavior.pickupBlock(user, level, pos, blockState);
    }

    static Optional<SoundEvent> getPickupSound(IBehaviorBlock block) {
        var behavior = block.getBehavior(BlockBehaviorTypes.WATERLOGGED);
        return behavior == null ? Optional.empty() : behavior.getPickupSound();
    }

    static Optional<SoundEvent> getPickupSound(IBehaviorBlock block, BlockState blockState) {
        var behavior = block.getBehavior(BlockBehaviorTypes.WATERLOGGED);
        return behavior == null ? Optional.empty() : behavior.getPickupSound(blockState);
    }

    static BlockState rotate(IBehaviorBlock block, BlockState blockState, Rotation rotation) {
        var result = blockState;

        for(var behavior : block.getBehaviors()) {
            result = behavior.rotate(result, rotation);
        }

        return result;
    }

    static BlockState mirror(IBehaviorBlock block, BlockState blockState, Mirror mirror) {
        var result = blockState;

        for(var behavior : block.getBehaviors()) {
            result = behavior.mirror(result, mirror);
        }

        return result;
    }
}
