package dev.apexstudios.apexcore.lib.component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class BaseComponent<TBase extends Component<TBase, TObj>, TObj> implements Component<TBase, TObj> {
    protected final ComponentHolder<TBase, TObj> holder;

    protected BaseComponent(ComponentHolder<TBase, TObj> holder) {
        this.holder = holder;
    }

    @Nullable
    @Override
    public final <TComponent extends TBase> TComponent getComponent(ComponentType<TBase, TComponent, TObj, ?> componentType) {
        return holder.getComponent(componentType);
    }

    @Override
    public final <TComponent extends TBase> Optional<TComponent> findComponent(ComponentType<TBase, TComponent, TObj, ?> componentType) {
        return holder.findComponent(componentType);
    }

    @Override
    public final <TComponent extends TBase> TComponent getComponentOrThrow(ComponentType<TBase, TComponent, TObj, ?> componentType) {
        return holder.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends TBase> void runForComponent(ComponentType<TBase, TComponent, TObj, ?> componentType, Consumer<TComponent> action) {
        holder.runForComponent(componentType, action);
    }

    @Override
    public final boolean hasComponent(ComponentType<TBase, ?, TObj, ?> componentType) {
        return holder.hasComponent(componentType);
    }

    @Override
    public final Set<ComponentType<TBase, ?, TObj, ?>> getComponentTypes() {
        return holder.getComponentTypes();
    }

    @Override
    public final Collection<TBase> getComponents() {
        return holder.getComponents();
    }

    @Override
    public final TObj unwrap() {
        return holder.unwrap();
    }
}
