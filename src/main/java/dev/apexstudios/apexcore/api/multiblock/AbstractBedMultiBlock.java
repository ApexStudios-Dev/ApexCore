package dev.apexstudios.apexcore.api.multiblock;

import java.util.OptionalDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBedMultiBlock extends AbstractBedBlock implements IMultiBlock {
    public AbstractBedMultiBlock(Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState().setValue(MULTI_BLOCK_INDEX, MULTI_BLOCK_ORIGIN_INDEX));
    }

    @Override
    protected EnvironmentAttribute<BedRule> getBedEnvironmentAttribute() {
        return EnvironmentAttributes.BED_RULE;
    }

    @Override
    protected InteractionResult destroyOnUse(BlockState blockState, Level level, BlockPos pos, Player player) {
        IMultiBlock.destroy(level, pos, blockState, player, this, false);

        var boomPos = Vec3.atCenterOf(pos);
        level.explode(null, level.damageSources().badRespawnPointExplosion(boomPos), null, boomPos, 5F, true, Level.ExplosionInteraction.BLOCK);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void destroyOnLeave(Level level, BlockPos pos) {
    }

    // region Patches
    @Override
    public OptionalDouble getSleepHeight(BlockState blockState, Level level, BlockPos pos) {
        return IMultiBlock.asOrigin(
                level,
                pos,
                blockState,
                this,
                (targetPos, targetBlockState) -> super.getSleepHeight(targetBlockState, level, targetPos),
                OptionalDouble::empty
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MULTI_BLOCK_INDEX);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockState = super.getStateForPlacement(context);
        return IMultiBlock.getStateForPlacement(context, blockState, this);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {
        // NOOP - Skip vanillas placement
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
        if(level.getBlockState(pos).is(blockState.getBlock())) {
            IMultiBlock.destroy(level, pos, blockState, null, this, false);
        }
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType type) {
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        if(neighbourBlockState.is(blockState.getBlock())) {
            var facing = blockState.getValue(FACING);

            if(facing.getAxis() == directionToNeighbour.getAxis()) {
                var occupied = neighbourBlockState.getValue(OCCUPIED);
                return blockState.setValue(OCCUPIED, occupied);
            }
        }

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
                () -> Blocks.AIR.defaultBlockState()
        );
    }
    // endregion
}
