package dev.apexstudios.apexcore.api.multiblock;

import java.util.HashSet;
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
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.BlockRelocability;
import org.jspecify.annotations.Nullable;

public interface IMultiBlock {
    int MULTI_BLOCK_ORIGIN_INDEX = 0;

    MultiBlockInfo multiBlockInfo();

    static BlockRelocability getRelocability(BlockPos pos, BlockState blockState, IMultiBlock multiBlock) {
        if(blockState.is(Tags.Blocks.RELOCATION_NOT_SUPPORTED)) {
            return BlockRelocability.No.INSTANCE;
        }

        var info = multiBlock.multiBlockInfo();
        var transformedLocal = info.getTransformed(blockState);
        var originPos = MultiBlockInfo.toOrigin(pos, transformedLocal);
        var positions = new HashSet<BlockPos>();
        var size = info.size();

        for(var i = 0; i < size; i++) {
            var targetBlockState = info.applyBlockState(blockState, i);
            transformedLocal = info.getTransformed(targetBlockState);
            var targetPos = MultiBlockInfo.fromOrigin(originPos, transformedLocal);

            positions.add(targetPos);
        }

        return new BlockRelocability.Multiblock(positions);
    }

    static void place(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, IMultiBlock multiBlock) {
        if(!isOrigin(blockState, multiBlock)) {
            return;
        }

        var info = multiBlock.multiBlockInfo();
        var size = info.size();
        var block = blockState.getBlock();

        for(var i = 1; i < size; i++) {
            var targetBlockState = info.applyBlockState(blockState, i);
            var transformedLocal = info.getTransformed(targetBlockState);
            var targetPos = MultiBlockInfo.fromOrigin(pos, transformedLocal);

            if(!level.getBlockState(targetPos).is(block)) {
                level.destroyBlock(targetPos, true, placer);
            }

            level.setBlockAndUpdate(targetPos, targetBlockState);
        }
    }

    static void destroy(LevelAccessor level, BlockPos pos, BlockState blockState, @Nullable LivingEntity breaker, IMultiBlock multiBlock, boolean dropBlocks) {
        var block = blockState.getBlock();
        var info = multiBlock.multiBlockInfo();

        if(isOrigin(blockState, multiBlock)) {
            var size = info.size();

            for(var i = 1; i < size; i++) {
                var targetBlockState = info.applyBlockState(blockState, i);
                var transformedLocal = info.getTransformed(targetBlockState);
                var targetPos = MultiBlockInfo.fromOrigin(pos, transformedLocal);
                var existingBlockState = level.getBlockState(targetPos);

                if(existingBlockState.is(block) && info.indexOf(existingBlockState) == i) {
                    level.removeBlock(targetPos, false);
                }
            }
        } else {
            var transformedLocal = info.getTransformed(blockState);
            var targetPos = MultiBlockInfo.toOrigin(pos, transformedLocal);
            var existingBlockState = level.getBlockState(targetPos);

            if(existingBlockState.is(block) && isOrigin(existingBlockState, multiBlock)) {
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
            var transformedLocal = info.getTransformed(targetBlockState);
            var targetPos = MultiBlockInfo.fromOrigin(pos, transformedLocal);

            if(!level.getBlockState(targetPos).canBeReplaced(context)) {
                return null;
            }
        }

        return result;
    }

    static boolean isSameMultiblock(
            BlockPos pos,
            BlockState blockState,
            BlockPos neighborPos,
            BlockState neighborBlockState,
            IMultiBlock multiBlock
    ) {
        if(!neighborBlockState.is(blockState.getBlock())) {
            return false;
        }

        var origin = pos;
        var neighborOrigin = neighborPos;

        if(!isOrigin(blockState, multiBlock)) {
            var transformedLocal = multiBlock.multiBlockInfo().getTransformed(blockState);
            origin = MultiBlockInfo.toOrigin(pos, transformedLocal);
        }

        if(!isOrigin(neighborBlockState, multiBlock)) {
            var transformedLocal = multiBlock.multiBlockInfo().getTransformed(neighborBlockState);
            neighborOrigin = MultiBlockInfo.toOrigin(neighborPos, transformedLocal);
        }

        return origin.equals(neighborOrigin);
    }

    static boolean isOrigin(BlockState blockState, IMultiBlock multiBlock) {
        return multiBlock.multiBlockInfo().indexOf(blockState) == MULTI_BLOCK_ORIGIN_INDEX;
    }

    static boolean isOrigin(BlockState blockState) {
        if(blockState.getBlock() instanceof IMultiBlock multiBlock) {
            return isOrigin(blockState, multiBlock);
        }

        return false;
    }

    static <TResult> TResult asOrigin(
            BlockGetter level,
            BlockPos pos,
            BlockState blockState,
            IMultiBlock multiBlock,
            BiFunction<BlockPos, BlockState, TResult> mapper,
            Supplier<TResult> invalidValue
    ) {
        if(isOrigin(blockState, multiBlock)) {
            return mapper.apply(pos, blockState);
        }

        var transformedLocal = multiBlock.multiBlockInfo().getTransformed(blockState);
        var targetPos = MultiBlockInfo.toOrigin(pos, transformedLocal);
        var existingBlockState = level.getBlockState(targetPos);

        if(!existingBlockState.is(blockState.getBlock()) || !isOrigin(existingBlockState, multiBlock)) {
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
            IMultiBlock multiBlock,
            BiFunction<BlockPos, BlockState, TResult> mapper
    ) {
        return asOrigin(level, pos, blockState, multiBlock, mapper, null);
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