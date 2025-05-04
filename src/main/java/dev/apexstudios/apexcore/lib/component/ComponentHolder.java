package dev.apexstudios.apexcore.lib.component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.ScheduledForRemoval
public interface ComponentHolder<TBase extends Component<TBase, TObj>, TObj> {
    @ApiStatus.NonExtendable
    @Nullable
    <TComponent extends TBase> TComponent getComponent(ComponentType<TBase, TComponent, TObj, ?> componentType);

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> Optional<TComponent> findComponent(ComponentType<TBase, TComponent, TObj, ?> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> TComponent getComponentOrThrow(ComponentType<TBase, TComponent, TObj, ?> componentType) {
        return Objects.requireNonNull(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> void runForComponent(ComponentType<TBase, TComponent, TObj, ?> componentType, Consumer<TComponent> action) {
        var component = getComponent(componentType);

        if(component != null)
            action.accept(component);
    }

    @ApiStatus.NonExtendable
    default boolean hasComponent(ComponentType<TBase, ?, TObj, ?> componentType) {
        return getComponent(componentType) != null;
    }

    @ApiStatus.NonExtendable
    Set<ComponentType<TBase, ?, TObj, ?>> getComponentTypes();

    @ApiStatus.NonExtendable
    Collection<TBase> getComponents();

    @ApiStatus.NonExtendable
    TObj unwrap();
}
