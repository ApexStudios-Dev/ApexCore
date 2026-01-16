package dev.apexstudios.apexcore.api.block.behavior;

import com.google.errorprone.annotations.ForOverride;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class BlockBehavior implements BlockBehaviorAccess {
    private final IBehaviorBlock owner;

    protected BlockBehavior(BlockBehaviorRegistration registration) {
        owner = registration.owner;
    }

    // region Callbacks
    @ForOverride
    protected BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourBlockState, RandomSource random) {
        return blockState;
    }

    @ForOverride
    protected InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @ForOverride
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    @ForOverride
    protected @Nullable BlockState getStateForPlacement(BlockState blockState, BlockPlaceContext context) {
        return blockState;
    }

    @ForOverride
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState;
    }

    @ForOverride
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState;
    }
    // endregion

    // region BlockBehaviorAccess
    @Override
    public final <TBehavior extends BlockBehavior> @Nullable TBehavior getBehavior(BlockBehaviorType<TBehavior, ?> type) {
        return owner.getBehavior(type);
    }

    @Override
    public final boolean hasBehavior(BlockBehaviorType<?, ?> type) {
        return owner.hasBehavior(type);
    }

    @Override
    public final Collection<BlockBehavior> getBehaviors() {
        return owner.getBehaviors();
    }

    @Override
    public final void forEachBehavior(Consumer<? super BlockBehavior> action) {
        owner.forEachBehavior(action);
    }

    @Override
    public final <TBehavior extends BlockBehavior> void executeIfPresent(BlockBehaviorType<TBehavior, ?> type, Consumer<TBehavior> action) {
        owner.executeIfPresent(type, action);
    }
    // endregion
}
