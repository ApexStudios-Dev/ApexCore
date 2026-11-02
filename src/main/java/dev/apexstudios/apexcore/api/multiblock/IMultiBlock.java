package dev.apexstudios.apexcore.api.multiblock;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.Nullable;

public interface IMultiBlock {
    int MAX_MULTI_BLOCK_SIZE = 255;
    IntegerProperty MULTI_BLOCK_INDEX = IntegerProperty.create("multi_block_index", 0, MAX_MULTI_BLOCK_SIZE);
    int MULTI_BLOCK_ORIGIN_INDEX = 0;

    MultiBlockInfo multiBlockInfo();

    static void place(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, IMultiBlock multiBlock) {
        if(!isOrigin(blockState)) {
            return;
        }

        var info = multiBlock.multiBlockInfo();
        var size = info.size();
        var block = blockState.getBlock();

        for(var i = 1; i < size; i++) {
            var targetBlockState = info.applyBlockState(blockState, i);
            var transformedLocal = info.getTransformed(targetBlockState, i);
            var targetPos = MultiBlockInfo.fromOrigin(pos, transformedLocal);
            var existingBlockState = level.getBlockState(targetPos);

            if(!level.getBlockState(targetPos).is(block)) {
                level.destroyBlock(targetPos, true, placer);
            }

            level.setBlockAndUpdate(targetPos, targetBlockState);
        }
    }

    static void destroy(LevelAccessor level, BlockPos pos, BlockState blockState, @Nullable LivingEntity breaker, IMultiBlock multiBlock, boolean dropBlocks) {
        var block = blockState.getBlock();
        var info = multiBlock.multiBlockInfo();

        if(isOrigin(blockState)) {
            var size = info.size();

            for(var i = 1; i < size; i++) {
                var targetBlockState = info.applyBlockState(blockState, i);
                var transformedLocal = info.getTransformed(targetBlockState, i);
                var targetPos = MultiBlockInfo.fromOrigin(pos, transformedLocal);
                var existingBlockState = level.getBlockState(targetPos);

                if(existingBlockState.is(block) && existingBlockState.getValue(MULTI_BLOCK_INDEX) == i) {
                    level.removeBlock(targetPos, false);
                }
            }
        } else {
            var index = blockState.getValue(MULTI_BLOCK_INDEX);
            var transformedLocal = info.getTransformed(blockState, index);
            var targetPos = MultiBlockInfo.toOrigin(pos, transformedLocal);
            var existingBlockState = level.getBlockState(targetPos);

            if(existingBlockState.is(block) && isOrigin(existingBlockState)) {
                if((!(breaker instanceof Player player) || !player.preventsBlockDrops()) && dropBlocks) {
                    Block.dropResources(existingBlockState, level, targetPos, level.getBlockEntity(targetPos));
                }

                level.removeBlock(targetPos, false);
            }
        }
    }

    static @Nullable BlockState getStateForPlacement(BlockPlaceContext context, @Nullable BlockState blockState, IMultiBlock multiBlock) {
        if(blockState == null) {
            return null;
        }

        var pos = context.getClickedPos();
        var level = context.getLevel();
        var info = multiBlock.multiBlockInfo();
        var size = info.size();
        var result = info.applyBlockState(blockState, MULTI_BLOCK_ORIGIN_INDEX);

        for(var i = 0; i < size; i++) {
            var targetBlockState = info.applyBlockState(result, i);
            var transformedLocal = info.getTransformed(targetBlockState, i);
            var targetPos = MultiBlockInfo.fromOrigin(pos, transformedLocal);

            if(!level.getBlockState(targetPos).canBeReplaced(context)) {
                return null;
            }
        }

        return result;
    }

    static boolean isOrigin(BlockState blockState) {
        return blockState.getValue(MULTI_BLOCK_INDEX) == MULTI_BLOCK_ORIGIN_INDEX;
    }

    static <TResult> TResult asOrigin(
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            IMultiBlock multiBlock,
            BiFunction<BlockPos, BlockState, TResult> mapper,
            Supplier<TResult> invalidValue
    ) {
        if(isOrigin(blockState)) {
            return mapper.apply(pos, blockState);
        }

        var index = blockState.getValue(MULTI_BLOCK_INDEX);
        var transformedLocal = multiBlock.multiBlockInfo().getTransformed(blockState, index);
        var targetPos = MultiBlockInfo.toOrigin(pos, transformedLocal);
        var existingBlockState = level.getBlockState(targetPos);

        if(!existingBlockState.is(blockState.getBlock()) || !isOrigin(existingBlockState)) {
            return invalidValue.get();
        }

        return mapper.apply(targetPos, existingBlockState);
    }

    static <TResult> TResult asOrigin(
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            BiFunction<BlockPos, BlockState, TResult> mapper,
            Supplier<TResult> invalidValue
    ) {
        if(blockState.getBlock() instanceof IMultiBlock multiBlock) {
            return asOrigin(level, pos, blockState, multiBlock, mapper, invalidValue);
        }

        return invalidValue.get();
    }

    static <TResult extends @Nullable Object> TResult asOrigin(
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            BiFunction<BlockPos, BlockState, TResult> mapper
    ) {
        return asOrigin(level, pos, blockState, mapper, null);
    }
}
