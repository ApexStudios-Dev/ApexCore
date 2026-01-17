package dev.apexstudios.apexcore.api.block.behavior;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

public class BehaviorBlock extends Block implements IBehaviorBlock, SimpleWaterloggedBlock {
    private final BlockBehaviorManager behaviorManager;

    public BehaviorBlock(Properties properties, Consumer<BlockBehaviorRegistrar> registrarCallback) {
        super(properties);

        behaviorManager = new BlockBehaviorManager(this, registrarCallback);
    }

    protected InteractionResult openMenu(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var menuProvider = blockState.getMenuProvider(level, pos);

        if(menuProvider != null) {
            player.openMenu(menuProvider);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // region Callbacks
    @Override
    @MustBeInvokedByOverriders
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        var result = super.updateShape(blockState, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourBlockState, random);
        return BlockBehaviorHelper.updateShape(this, result, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourBlockState, random);
    }

    @Override
    @MustBeInvokedByOverriders
    protected InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        var result = super.useItemOn(stack, blockState, level, pos, player, hand, hitResult);

        if(!result.consumesAction()) {
            result = BlockBehaviorHelper.useItemOn(this, stack, blockState, level, pos, player, hand, hitResult);
        }

        return result;
    }

    @Override
    @MustBeInvokedByOverriders
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var result = super.useWithoutItem(blockState, level, pos, player, hitResult);

        if(!result.consumesAction()) {
            result = BlockBehaviorHelper.useWithoutItem(this, blockState, level, pos, player, hitResult);
        }

        if(!result.consumesAction()) {
            result = openMenu(blockState, level, pos, player, hitResult);
        }

        return result;
    }

    @Override
    @MustBeInvokedByOverriders
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockState = super.getStateForPlacement(context);

        if(blockState != null && !blockState.isEmpty()) {
            blockState = BlockBehaviorHelper.getStateForPlacement(this, blockState, context);
        }

        return blockState != null && blockState.isEmpty() ? null : blockState;
    }

    @Override
    protected final FluidState getFluidState(BlockState blockState) {
        var fluidState = BlockBehaviorHelper.getFluidState(this, blockState);
        return fluidState == null ? super.getFluidState(blockState) : fluidState;
    }

    @Override
    public final boolean canPlaceLiquid(@Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState blockState, Fluid fluid) {
        return BlockBehaviorHelper.canPlaceLiquid(this, user, level, pos, blockState, fluid);
    }

    @Override
    public final boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        return BlockBehaviorHelper.placeLiquid(this, level, pos, blockState, fluidState);
    }

    @Override
    public final ItemStack pickupBlock(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos, BlockState blockState) {
        return BlockBehaviorHelper.pickupBlock(this, user, level, pos, blockState);
    }

    @Override
    public final Optional<SoundEvent> getPickupSound() {
        return BlockBehaviorHelper.getPickupSound(this);
    }

    @Override
    public final Optional<SoundEvent> getPickupSound(BlockState blockState) {
        return BlockBehaviorHelper.getPickupSound(this, blockState);
    }

    @Override
    @MustBeInvokedByOverriders
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        var result = super.rotate(blockState, rotation);
        return BlockBehaviorHelper.rotate(this, result, rotation);
    }

    @Override
    @MustBeInvokedByOverriders
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        var result = super.mirror(blockState, mirror);
        return BlockBehaviorHelper.mirror(this, result, mirror);
    }
    // endregion

    // region BlockBehaviorAccess
    @Override
    public final <TBehavior extends BlockBehavior> @Nullable TBehavior getBehavior(BlockBehaviorType<TBehavior> type) {
        return behaviorManager.getBehavior(type);
    }

    @Override
    public final boolean hasBehavior(BlockBehaviorType<?> type) {
        return behaviorManager.hasBehavior(type);
    }

    @Override
    public final Collection<BlockBehavior> getBehaviors() {
        return behaviorManager.getBehaviors();
    }

    @Override
    public final void forEachBehavior(Consumer<? super BlockBehavior> action) {
        behaviorManager.forEachBehavior(action);
    }

    @Override
    public final <TBehavior extends BlockBehavior> void executeIfPresent(BlockBehaviorType<TBehavior> type, Consumer<TBehavior> action) {
        behaviorManager.executeIfPresent(type, action);
    }
    // endregion

    public static abstract class WithEntity extends BehaviorBlock implements EntityBlock {
        public WithEntity(Properties properties, Consumer<BlockBehaviorRegistrar> registrarCallback) {
            super(properties, registrarCallback);
        }

        @Override
        public abstract @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState blockState);

        @Override
        protected boolean triggerEvent(BlockState blockState, Level level, BlockPos pos, int b0, int b1) {
            if(super.triggerEvent(blockState, level, pos, b0, b1)) {
                return true;
            }

            var blockEntity = level.getBlockEntity(pos);
            return blockEntity != null && blockEntity.triggerEvent(b0, b1);
        }

        @Override
        protected @Nullable MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos pos) {
            var provider = super.getMenuProvider(blockState, level, pos);

            if(provider == null && level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
                provider = menuProvider;
            }

            return provider;
        }
    }
}
