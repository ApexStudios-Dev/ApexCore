package dev.apexstudios.apexcore.api.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

public interface DelegateBlockGetter extends DelegateLevelHeightAccessor, BlockGetter {
    @Override
    BlockGetter delegate();

    @Override
    default @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return delegate().getBlockEntity(pos);
    }

    @Override
    default BlockState getBlockState(BlockPos pos) {
        return delegate().getBlockState(pos);
    }

    @Override
    default FluidState getFluidState(BlockPos pos) {
        return delegate().getFluidState(pos);
    }

    @Override
    default ModelData getModelData(BlockPos pos) {
        return delegate().getModelData(pos);
    }

    static DelegateBlockGetter wrap(BlockGetter level) {
        return level instanceof DelegateBlockGetter delegate ? delegate : () -> level;
    }

    static BlockGetter unwrap(BlockGetter level) {
        if(level instanceof DelegateBlockGetter delegate) {
            return unwrap(delegate.delegate());
        }

        return level;
    }
}
