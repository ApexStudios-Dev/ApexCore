package dev.apexstudios.apexcore.lib.component;

import java.util.function.UnaryOperator;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ComponentRegistrar<TBase extends Component<TBase, TObj>, TObj> {
    <TComponent extends TBase, TBuilder extends ComponentBuilder> ComponentRegistrar<TBase, TObj> register(ComponentType<TBase, TComponent, TObj, TBuilder> componentType, UnaryOperator<TBuilder> builder);

    default ComponentRegistrar<TBase, TObj> register(ComponentType<TBase, ?, TObj, ?>... componentTypes) {
        for(var componentType : componentTypes) {
            register(componentType, UnaryOperator.identity());
        }

        return this;
    }
}
