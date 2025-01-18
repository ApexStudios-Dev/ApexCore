package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface DelegatedScheduledTickAccess extends ScheduledTickAccess {
    ScheduledTickAccess delegate();

    @Override
    default <T> ScheduledTick<T> createTick(BlockPos pos, T type, int delay, TickPriority priority) {
        return delegate().createTick(pos, type, delay, priority);
    }

    @Override
    default <T> ScheduledTick<T> createTick(BlockPos pos, T type, int delay) {
        return delegate().createTick(pos, type, delay);
    }

    @Override
    default LevelTickAccess<Block> getBlockTicks() {
        return delegate().getBlockTicks();
    }

    @Override
    default void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) {
        delegate().scheduleTick(pos, block, delay, priority);
    }

    @Override
    default void scheduleTick(BlockPos pos, Block block, int delay) {
        delegate().scheduleTick(pos, block, delay);
    }

    @Override
    default LevelTickAccess<Fluid> getFluidTicks() {
        return delegate().getFluidTicks();
    }

    @Override
    default void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) {
        delegate().scheduleTick(pos, fluid, delay, priority);
    }

    @Override
    default void scheduleTick(BlockPos pos, Fluid fluid, int delay) {
        delegate().scheduleTick(pos, fluid, delay);
    }
}
