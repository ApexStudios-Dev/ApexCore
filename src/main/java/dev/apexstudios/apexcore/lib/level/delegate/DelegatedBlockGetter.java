package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jetbrains.annotations.Nullable;

public interface DelegatedBlockGetter extends BlockGetter, DelegatedLevelHeightAccessor {
    @Override
    BlockGetter delegate();

    @Override
    @Nullable
    default BlockEntity getBlockEntity(BlockPos pos) {
        return delegate().getBlockEntity(pos);
    }

    @Override
    default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> blockEntityType) {
        return delegate().getBlockEntity(pos, blockEntityType);
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
    default int getLightEmission(BlockPos pos) {
        return delegate().getLightEmission(pos);
    }

    @Override
    default Stream<BlockState> getBlockStates(AABB area) {
        return delegate().getBlockStates(area);
    }

    @Override
    default BlockHitResult isBlockInLine(ClipBlockStateContext context) {
        return delegate().isBlockInLine(context);
    }

    @Override
    default BlockHitResult clip(ClipContext context) {
        return delegate().clip(context);
    }

    @Override
    @Nullable
    default BlockHitResult clipWithInteractionOverride(Vec3 startVec, Vec3 endVec, BlockPos pos, VoxelShape shape, BlockState state) {
        return delegate().clipWithInteractionOverride(startVec, endVec, pos, shape, state);
    }

    @Override
    default double getBlockFloorHeight(VoxelShape shape, Supplier<VoxelShape> belowShapeSupplier) {
        return delegate().getBlockFloorHeight(shape, belowShapeSupplier);
    }

    @Override
    default double getBlockFloorHeight(BlockPos pos) {
        return delegate().getBlockFloorHeight(pos);
    }

    @Override
    default @Nullable AuxiliaryLightManager getAuxLightManager(BlockPos pos) {
        return delegate().getAuxLightManager(pos);
    }

    @Override
    default @Nullable AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        return delegate().getAuxLightManager(pos);
    }

    @Override
    default ModelData getModelData(BlockPos pos) {
        return delegate().getModelData(pos);
    }
}
