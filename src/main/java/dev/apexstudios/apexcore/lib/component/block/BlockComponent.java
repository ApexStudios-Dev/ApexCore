package dev.apexstudios.apexcore.lib.component.block;

import com.google.errorprone.annotations.ForOverride;
import dev.apexstudios.apexcore.lib.component.Component;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public interface BlockComponent extends Component<
        BlockComponent,
        Block,
        BlockComponentHolder,
        BlockComponentType<? extends BlockComponent, ?>
>, BlockComponentHolder {
    @ForOverride
    default BlockState registerDefaultBlockState(BlockState blockState) {
        return blockState;
    }

    @ForOverride
    default void createBlockStateDefinition(Consumer<Property<?>> consumer) {

    }

    @ForOverride
    @Nullable
    default BlockState getStateForPlacement(BlockPlaceContext context, BlockState blockState) {
        return blockState;
    }

    @ForOverride
    default boolean hasAnalogOutputSignal(BlockState blockState) {
        return false;
    }

    @ForOverride
    default boolean isPathfindable(BlockState blockState, PathComputationType pathType) {
        return true;
    }

    @ForOverride
    default void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {

    }

    @ForOverride
    default BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState;
    }

    @ForOverride
    default BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState;
    }

    @ForOverride
    default FluidState getFluidState(BlockState blockState, FluidState fluidState) {
        return fluidState;
    }

    @ForOverride
    default void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel level, BlockPos pos, boolean movedByPiston) {

    }

    @ForOverride
    default Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState blockState, EntityType<?> entityType, LevelReader level, BlockPos pos, float orientation) {
        return Optional.empty();
    }

    @ForOverride
    default void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {

    }

    @ForOverride
    default BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        return blockState;
    }

    @ForOverride
    default void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {

    }

    @ForOverride
    default InteractionResult useItemOn(ItemStack stack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        return InteractionResult.PASS;
    }

    @ForOverride
    default InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos pos, Player player, BlockHitResult result) {
        return InteractionResult.PASS;
    }

    @ForOverride
    default int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        return 0;
    }

    @ForOverride
    default void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier) {

    }

    @ForOverride
    default void handlePrecipitation(BlockState blockState, Level level, BlockPos pos, Biome.Precipitation precipitation) {

    }

    @ForOverride
    default void stepOn(Level level, BlockPos pos, BlockState blockState, Entity entity) {

    }

    @ForOverride
    default boolean updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        return false;
    }

    @ForOverride
    default void modifyCloneItemStack(ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData) {

    }

    @ForOverride
    default void modifyCloneItemStack(ItemStack stack, LevelReader level, BlockPos pos, BlockState blockState, boolean includeData, Player player) {
        modifyCloneItemStack(stack, level, pos, blockState, includeData);
    }
}
