package dev.apexstudios.apexcore.lib.block;

import com.google.errorprone.annotations.ForOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Common events between Block -> BlockEntity
public interface BlockEvents {
    @ForOverride
    default void playerDestroy(Level level, Player player, BlockPos pos, BlockState blockState, ItemStack stack) {

    }

    @ForOverride
    default void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack) {

    }

    @ForOverride
    default BlockState playerWillDestroy(Level level, BlockPos pos, BlockState blockState, Player player) {
        return blockState;
    }

    @ForOverride
    default BlockState updateShape(BlockState blockState, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction facing, BlockPos neighborPos, BlockState neighborBlockState, RandomSource random) {
        return blockState;
    }

    @ForOverride
    default void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {

    }

    @ForOverride
    default void onPlace(BlockState blockState, Level level, BlockPos pos, BlockState oldBlockState, boolean movedByPiston) {

    }

    @ForOverride
    default void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean movedByPiston) {

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
    default void entityInside(BlockState blockState, Level level, BlockPos pos, Entity entity) {

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
