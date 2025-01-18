package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface DelegatedLevelWriter extends LevelWriter {
    LevelWriter delegate();

    @Override
    default boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return delegate().setBlock(pos, state, flags, recursionLeft);
    }

    @Override
    default boolean setBlock(BlockPos pos, BlockState newState, int flags) {
        return delegate().setBlock(pos, newState, flags);
    }

    @Override
    default boolean removeBlock(BlockPos pos, boolean isMoving) {
        return delegate().removeBlock(pos, isMoving);
    }

    @Override
    default boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return delegate().destroyBlock(pos, dropBlock);
    }

    @Override
    default boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity) {
        return delegate().destroyBlock(pos, dropBlock, entity);
    }

    @Override
    default boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return delegate().destroyBlock(pos, dropBlock, entity, recursionLeft);
    }

    @Override
    default boolean addFreshEntity(Entity entity) {
        return delegate().addFreshEntity(entity);
    }
}
