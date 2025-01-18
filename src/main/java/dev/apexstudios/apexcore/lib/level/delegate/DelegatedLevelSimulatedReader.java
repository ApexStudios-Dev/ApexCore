package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

public interface DelegatedLevelSimulatedReader extends LevelSimulatedReader {
    LevelSimulatedReader delegate();

    @Override
    default boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) {
        return delegate().isStateAtPosition(pos, state);
    }

    @Override
    default boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
        return delegate().isFluidAtPosition(pos, predicate);
    }

    @Override
    default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return delegate().getBlockEntity(pos, type);
    }

    @Override
    default BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return delegate().getHeightmapPos(heightmapType, pos);
    }
}
