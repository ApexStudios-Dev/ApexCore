package dev.apexstudios.apexcore.api.block.behavior;

import java.util.Collection;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public interface BlockBehaviorAccess {
    <TBehavior extends BlockBehavior> @Nullable TBehavior getBehavior(BlockBehaviorType<TBehavior> type);

    default boolean hasBehavior(BlockBehaviorType<?> type) {
        return getBehavior(type) != null;
    }

    Collection<BlockBehavior> getBehaviors();

    default void forEachBehavior(Consumer<? super BlockBehavior> action) {
        getBehaviors().forEach(action);
    }

    default <TBehavior extends BlockBehavior> void executeIfPresent(BlockBehaviorType<TBehavior> type, Consumer<TBehavior> action) {
        var behavior = getBehavior(type);

        if(behavior != null) {
            action.accept(behavior);
        }
    }
}
