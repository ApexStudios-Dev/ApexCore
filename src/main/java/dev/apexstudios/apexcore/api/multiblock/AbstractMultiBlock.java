package dev.apexstudios.apexcore.api.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.Nullable;

public abstract class AbstractMultiBlock extends Block implements IMultiBlock {
    public AbstractMultiBlock(Properties properties) {
        super(properties);

        registerDefaultState(multiBlockInfo().setIndex(defaultBlockState(), MULTI_BLOCK_ORIGIN_INDEX));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(multiBlockInfo().property());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockState = super.getStateForPlacement(context);
        return IMultiBlock.getStateForPlacement(context, blockState, this);
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {
        if(!oldBlockState.is(blockState.getBlock())) {
            IMultiBlock.place(level, pos, blockState, null, this);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        IMultiBlock.destroy(level, pos, blockState, player, this, true);
        return super.playerWillDestroy(level, pos, blockState, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if(!level.getBlockState(pos).is(blockState.getBlock())) {
            IMultiBlock.destroy(level, pos, blockState, null, this, false);
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        // check other properties (bed copies over occupied here)

        return IMultiBlock.asOrigin(
                level,
                pos,
                blockState,
                this,
                (targetPos, targetBlockState) -> {
                    if(targetBlockState.is(this)) {
                        return blockState;
                    }

                    return Blocks.AIR.defaultBlockState();
                },
                Blocks.AIR::defaultBlockState
        );
    }
}
