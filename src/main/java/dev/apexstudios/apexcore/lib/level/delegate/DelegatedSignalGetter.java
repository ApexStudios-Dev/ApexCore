package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;

public interface DelegatedSignalGetter extends DelegatedBlockGetter, SignalGetter {
    @Override
    SignalGetter delegate();

    @Override
    default int getDirectSignal(BlockPos pos, Direction direction) {
        return delegate().getDirectSignal(pos, direction);
    }

    @Override
    default int getDirectSignalTo(BlockPos pos) {
        return delegate().getDirectSignalTo(pos);
    }

    @Override
    default int getControlInputSignal(BlockPos pos, Direction direction, boolean diodesOnly) {
        return delegate().getControlInputSignal(pos, direction, diodesOnly);
    }

    @Override
    default boolean hasSignal(BlockPos pos, Direction direction) {
        return delegate().hasSignal(pos, direction);
    }

    @Override
    default int getSignal(BlockPos pos, Direction direction) {
        return delegate().getSignal(pos, direction);
    }

    @Override
    default boolean hasNeighborSignal(BlockPos pos) {
        return delegate().hasNeighborSignal(pos);
    }

    @Override
    default int getBestNeighborSignal(BlockPos pos) {
        return delegate().getBestNeighborSignal(pos);
    }
}
