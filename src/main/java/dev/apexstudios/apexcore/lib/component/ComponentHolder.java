package dev.apexstudios.apexcore.lib.component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface ComponentHolder<TBase extends Component<TBase>> {
    @ApiStatus.NonExtendable
    @Nullable
    <TComponent extends TBase> TComponent getComponent(ComponentType<TBase, TComponent, ?> componentType);

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> Optional<TComponent> findComponent(ComponentType<TBase, TComponent, ?> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> TComponent getComponentOrThrow(ComponentType<TBase, TComponent, ?> componentType) {
        return Objects.requireNonNull(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> void runForComponent(ComponentType<TBase, TComponent, ?> componentType, Consumer<TComponent> action) {
        var component = getComponent(componentType);

        if(component != null)
            action.accept(component);
    }

    @ApiStatus.NonExtendable
    default boolean hasComponent(ComponentType<TBase, ?, ?> componentType) {
        return getComponent(componentType) != null;
    }

    @ApiStatus.NonExtendable
    Set<ComponentType<TBase, ?, ?>> getComponentTypes();

    @ApiStatus.NonExtendable
    Collection<TBase> getComponents();
}
