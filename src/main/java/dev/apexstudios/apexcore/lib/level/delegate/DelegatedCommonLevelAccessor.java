package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface DelegatedCommonLevelAccessor extends DelegatedEntityGetter, DelegatedLevelReader, DelegatedLevelSimulatedRW, CommonLevelAccessor {
    @Override
    CommonLevelAccessor delegate();

    @Override
    default List<VoxelShape> getEntityCollisions(@Nullable Entity p_entity, AABB collisionBox) {
        return delegate().getEntityCollisions(p_entity, collisionBox);
    }

    @Override
    default BlockPos getHeightmapPos(Heightmap.Types heightmapType, BlockPos pos) {
        return delegate().getHeightmapPos(heightmapType, pos);
    }

    @Override
    default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> blockEntityType) {
        return delegate().getBlockEntity(pos, blockEntityType);
    }

    @Override
    default boolean isUnobstructed(@Nullable Entity p_entity, VoxelShape shape) {
        return delegate().isUnobstructed(p_entity, shape);
    }
}
