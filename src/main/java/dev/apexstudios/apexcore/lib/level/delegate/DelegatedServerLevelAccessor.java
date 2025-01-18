package dev.apexstudios.apexcore.lib.level.delegate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;

public interface DelegatedServerLevelAccessor extends DelegatedLevelAccessor, ServerLevelAccessor {
    @Override
    ServerLevelAccessor delegate();

    @Override
    default ServerLevel getLevel() {
        return delegate().getLevel();
    }

    @Override
    default void addFreshEntityWithPassengers(Entity entity) {
        delegate().addFreshEntityWithPassengers(entity);
    }
}
