package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.List;
import net.minecraft.server.level.ServerEntityGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public interface DelegatedServerEntityGetter extends DelegatedEntityGetter, ServerEntityGetter {
    @Override
    ServerEntityGetter delegate();

    @Override
    default ServerLevel getLevel() {
        return delegate().getLevel();
    }

    @Override
    @Nullable
    default Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity source) {
        return delegate().getNearestPlayer(targetingConditions, source);
    }

    @Override
    @Nullable
    default Player getNearestPlayer(TargetingConditions targetingConditions, LivingEntity source, double x, double y, double z) {
        return delegate().getNearestPlayer(targetingConditions, source, x, y, z);
    }

    @Override
    @Nullable
    default Player getNearestPlayer(TargetingConditions targetingConditions, double x, double y, double z) {
        return delegate().getNearestPlayer(targetingConditions, x, y, z);
    }

    @Override
    @Nullable
    default <T extends LivingEntity> T getNearestEntity(Class<? extends T> entityClass, TargetingConditions targetingConditions, @Nullable LivingEntity source, double x, double y, double z, AABB area) {
        return delegate().getNearestEntity(entityClass, targetingConditions, source, x, y, z, area);
    }

    @Override
    @Nullable
    default <T extends LivingEntity> T getNearestEntity(List<? extends T> entities, TargetingConditions targetingConditions, @Nullable LivingEntity source, double x, double y, double z) {
        return delegate().getNearestEntity(entities, targetingConditions, source, x, y, z);
    }

    @Override
    default List<Player> getNearbyPlayers(TargetingConditions targetingConditions, LivingEntity source, AABB area) {
        return delegate().getNearbyPlayers(targetingConditions, source, area);
    }

    @Override
    default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> entityClass, TargetingConditions targetingConditions, LivingEntity source, AABB area) {
        return delegate().getNearbyEntities(entityClass, targetingConditions, source, area);
    }
}
