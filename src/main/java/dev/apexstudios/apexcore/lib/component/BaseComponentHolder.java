package dev.apexstudios.apexcore.lib.component;

import com.google.errorprone.annotations.ForOverride;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

// Provided as an example implementation for ComponentHolder
// Safe to extended but for use cases where you cant extend this class directly
// try to implement ComponentHolder manually while copying this implementation as close as possible
//
// See BaseBlockComponentHolder & BaseEntityBlockComponentHolder for Block ComponentHolder implementations
// See BaseBlockEntityComponentHolder for BlockEntity ComponentHolder implementation
public class BaseComponentHolder<TBase extends Component<TBase>> implements ComponentHolder<TBase> {
    private final Map<ComponentType<TBase, ?, ?>, TBase> components = ComponentHelper.registerComponents(this, BaseComponentHolder::registerComponents);

    // region: ComponentHolder
    @ForOverride
    protected void registerComponents(ComponentRegistrar<TBase> registrar) {

    }

    @Nullable
    @Override
    public final <TComponent extends TBase> TComponent getComponent(ComponentType<TBase, TComponent, ?> componentType) {
        return (TComponent) components.get(componentType);
    }

    @Override
    public final <TComponent extends TBase> Optional<TComponent> findComponent(ComponentType<TBase, TComponent, ?> componentType) {
        return ComponentHolder.super.findComponent(componentType);
    }

    @Override
    public final <TComponent extends TBase> TComponent getComponentOrThrow(ComponentType<TBase, TComponent, ?> componentType) {
        return ComponentHolder.super.getComponentOrThrow(componentType);
    }

    @Override
    public final <TComponent extends TBase> void runForComponent(ComponentType<TBase, TComponent, ?> componentType, Consumer<TComponent> action) {
        ComponentHolder.super.runForComponent(componentType, action);
    }

    @Override
    public final boolean hasComponent(ComponentType<TBase, ?, ?> componentType) {
        return ComponentHolder.super.hasComponent(componentType);
    }

    @Override
    public final Set<ComponentType<TBase, ?, ?>> getComponentTypes() {
        return components.keySet();
    }

    @Override
    public final Collection<TBase> getComponents() {
        return components.values();
    }
    // endregion
}
