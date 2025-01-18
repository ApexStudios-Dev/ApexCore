package dev.apexstudios.apexcore.lib.level.delegate;

import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public interface DelegatedLevelEntityGetter<T extends EntityAccess> extends LevelEntityGetter<T> {
    LevelEntityGetter<T> delegate();

    @Override
    @Nullable
    default T get(int id) {
        return delegate().get(id);
    }

    @Override
    @Nullable
    default T get(UUID uuid) {
        return delegate().get(uuid);
    }

    @Override
    default Iterable<T> getAll() {
        return delegate().getAll();
    }

    @Override
    default <U extends T> void get(EntityTypeTest<T, U> test, AbortableIterationConsumer<U> consumer) {
        delegate().get(test, consumer);
    }

    @Override
    default void get(AABB boundingBox, Consumer<T> consumer) {
        delegate().get(boundingBox, consumer);
    }

    @Override
    default <U extends T> void get(EntityTypeTest<T, U> test, AABB bounds, AbortableIterationConsumer<U> consumer) {
        delegate().get(test, bounds, consumer);
    }
}
