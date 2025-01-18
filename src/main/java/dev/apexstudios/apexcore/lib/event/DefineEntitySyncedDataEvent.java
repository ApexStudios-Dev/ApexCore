package dev.apexstudios.apexcore.lib.event;

import java.util.function.Supplier;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;

// TODO: PR into NeoForge
public final class DefineEntitySyncedDataEvent extends EntityEvent {
    private final SynchedEntityData.Builder builder;

    @ApiStatus.Internal
    public DefineEntitySyncedDataEvent(Entity entity, SynchedEntityData.Builder builder) {
        super(entity);

        this.builder = builder;
    }

    public <TData> void define(EntityDataAccessor<TData> accessor, TData value) {
        builder.define(accessor, value);
    }

    public <TData> void define(Supplier<? extends EntityDataAccessor<TData>> accessor, TData value) {
        define(accessor.get(), value);
    }

    public <TData> void define(EntityType<?> entityType, EntityDataAccessor<TData> accessor, Supplier<TData> value) {
        if(entityType.tryCast(getEntity()) != null)
            define(accessor, value.get());
    }
}
