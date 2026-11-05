package dev.apexstudios.apexcore.api.multiblock;

import dev.apexstudios.apexcore.api.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.util.BlockRelocability;
import org.jspecify.annotations.Nullable;

public abstract class AbstractDoorMultiBlock extends DoorBlock implements IMultiBlock {
    public AbstractDoorMultiBlock(BlockSetType type, Properties properties) {
        super(type, properties);

        registerDefaultState(multiBlockInfo().setIndex(defaultBlockState(), MULTI_BLOCK_ORIGIN_INDEX));
    }

    // region Patches
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        // NOOP - Skip vanillas placement
        System.out.println("boop");
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

        // skip AbstractBedBlock.playerWillDestroy
        // same logic as BlockBehavior
        spawnDestroyByEntityParticles(level, player, pos, blockState);

        if(blockState.is(BlockTags.GUARDED_BY_PIGLINS) && level instanceof ServerLevel sLevel) {
            PiglinAi.angerNearbyPiglins(sLevel, player, false);
        }

        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(player, blockState));
        return blockState;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if(!level.getBlockState(pos).is(blockState.getBlock())) {
            IMultiBlock.destroy(level, pos, blockState, null, this, false);
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        if(!IMultiBlock.isSameMultiblock(pos, blockState, neighbourPos, neighbourBlockState, this)) {
            return blockState;
        }

        if(directionToNeighbour.getAxis() != Direction.Axis.Y) {
            return Blocks.AIR.defaultBlockState();
        }

        if(blockState.getValue(HALF) == DoubleBlockHalf.LOWER && directionToNeighbour == Direction.DOWN && !blockState.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return BlockHelper.copyProperties(
                neighbourBlockState,
                blockState,
                OPEN,
                POWERED,
                HINGE
        );
    }

    @Override
    public BlockRelocability getRelocability(LevelReader level, BlockPos pos, BlockState state) {
        return IMultiBlock.getRelocability(pos, defaultBlockState(), this);
    }
    // endregion
}
