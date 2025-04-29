package dev.apexstudios.apexcore.lib.component;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface ComponentHolder<
        TBase extends Component<TBase, TObj, THolder, TType>,
        TObj,
        THolder extends ComponentHolder<TBase, TObj, THolder, TType>,
        TType extends ComponentType<TBase, ? extends TBase, TObj, THolder, TType, ?>
> {
    @ApiStatus.NonExtendable
    @Nullable
    <TComponent extends TBase> TComponent getComponent(ComponentType<TBase, TComponent, TObj, THolder, TType, ?> componentType);

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> Optional<TComponent> findComponent(ComponentType<TBase, TComponent, TObj, THolder, TType, ?> componentType) {
        return Optional.ofNullable(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> TComponent getComponentOrThrow(ComponentType<TBase, TComponent, TObj, THolder, TType, ?> componentType) {
        return Objects.requireNonNull(getComponent(componentType));
    }

    @ApiStatus.NonExtendable
    default <TComponent extends TBase> void runForComponent(ComponentType<TBase, TComponent, TObj, THolder, TType, ?> componentType, Consumer<TComponent> action) {
        var component = getComponent(componentType);

        if(component != null)
            action.accept(component);
    }

    @ApiStatus.NonExtendable
    boolean hasComponent(TType componentType);

    @ApiStatus.NonExtendable
    Set<TType> getComponentTypes();

    @ApiStatus.NonExtendable
    Collection<TBase> getComponents();

    @ApiStatus.NonExtendable
    TObj unwrap();
}
