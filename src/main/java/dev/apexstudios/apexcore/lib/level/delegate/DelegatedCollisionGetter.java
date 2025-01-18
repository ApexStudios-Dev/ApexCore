package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface DelegatedCollisionGetter extends DelegatedBlockGetter, CollisionGetter {
    @Override
    CollisionGetter delegate();

    @Override
    default WorldBorder getWorldBorder() {
        return delegate().getWorldBorder();
    }

    @Override
    @Nullable
    default BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
        return delegate().getChunkForCollisions(chunkX, chunkZ);
    }

    @Override
    default boolean isUnobstructed(@Nullable Entity entity, VoxelShape shape) {
        return delegate().isUnobstructed(entity, shape);
    }

    @Override
    default boolean isUnobstructed(BlockState state, BlockPos pos, CollisionContext context) {
        return delegate().isUnobstructed(state, pos, context);
    }

    @Override
    default boolean isUnobstructed(Entity entity) {
        return delegate().isUnobstructed(entity);
    }

    @Override
    default boolean noCollision(AABB collisionBox) {
        return delegate().noCollision(collisionBox);
    }

    @Override
    default boolean noCollision(Entity entity) {
        return delegate().noCollision(entity);
    }

    @Override
    default boolean noCollision(@Nullable Entity entity, AABB collisionBox) {
        return delegate().noCollision(entity, collisionBox);
    }

    @Override
    default boolean noCollision(@Nullable Entity entity, AABB collisionBox, boolean checkLiquid) {
        return delegate().noCollision(entity, collisionBox, checkLiquid);
    }

    @Override
    default boolean noBlockCollision(@Nullable Entity entity, AABB boundingBox) {
        return delegate().noBlockCollision(entity, boundingBox);
    }

    @Override
    default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate().getEntityCollisions(entity, collisionBox);
    }

    @Override
    default Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate().getCollisions(entity, collisionBox);
    }

    @Override
    default Iterable<VoxelShape> getBlockCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate().getBlockCollisions(entity, collisionBox);
    }

    @Override
    default Iterable<VoxelShape> getBlockAndLiquidCollisions(@Nullable Entity entity, AABB collisionBox) {
        return delegate().getBlockAndLiquidCollisions(entity, collisionBox);
    }

    @Override
    default BlockHitResult clipIncludingBorder(ClipContext clipContext) {
        return delegate().clipIncludingBorder(clipContext);
    }

    @Override
    default boolean collidesWithSuffocatingBlock(@Nullable Entity entity, AABB box) {
        return delegate().collidesWithSuffocatingBlock(entity, box);
    }

    @Override
    default Optional<BlockPos> findSupportingBlock(Entity entity, AABB box) {
        return delegate().findSupportingBlock(entity, box);
    }

    @Override
    default Optional<Vec3> findFreePosition(@Nullable Entity entity, VoxelShape shape, Vec3 pos, double x, double y, double z) {
        return delegate().findFreePosition(entity, shape, pos, x, y, z);
    }
}
