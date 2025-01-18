package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface DelegatedEntityGetter extends EntityGetter {
    EntityGetter delegate();

    @Override
    default List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate) {
        return delegate().getEntities(entity, area, predicate);
    }

    @Override
    default <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate) {
        return delegate().getEntities(entityTypeTest, bounds, predicate);
    }

    @Override
    default <T extends Entity> List<T> getEntitiesOfClass(Class<T> entityClass, AABB area, Predicate<? super T> filter) {
        return delegate().getEntitiesOfClass(entityClass, area, filter);
    }

    @Override
    default List<? extends Player> players() {
        return delegate().players();
    }

    @Override
    default List<Entity> getEntities(@Nullable Entity entity, AABB area) {
        return delegate().getEntities(entity, area);
    }

    @Override
    default boolean isUnobstructed(@Nullable Entity p_entity, VoxelShape shape) {
        return delegate().isUnobstructed(p_entity, shape);
    }

    @Override
    default <T extends Entity> List<T> getEntitiesOfClass(Class<T> entityClass, AABB area) {
        return delegate().getEntitiesOfClass(entityClass, area);
    }

    @Override
    default List<VoxelShape> getEntityCollisions(@Nullable Entity p_entity, AABB collisionBox) {
        return delegate().getEntityCollisions(p_entity, collisionBox);
    }

    @Override
    @Nullable
    default Player getNearestPlayer(double x, double y, double z, double distance, @Nullable Predicate<Entity> predicate) {
        return delegate().getNearestPlayer(x, y, z, distance, predicate);
    }

    @Override
    @Nullable
    default Player getNearestPlayer(Entity entity, double distance) {
        return delegate().getNearestPlayer(entity, distance);
    }

    @Override
    @Nullable
    default Player getNearestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return delegate().getNearestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    default boolean hasNearbyAlivePlayer(double x, double y, double z, double distance) {
        return delegate().hasNearbyAlivePlayer(x, y, z, distance);
    }

    @Override
    @Nullable
    default Player getPlayerByUUID(UUID uniqueId) {
        return delegate().getPlayerByUUID(uniqueId);
    }
}
