package dev.apexstudios.apexcore.lib.component;

import java.util.function.UnaryOperator;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ComponentRegistrar<TBase extends Component<TBase>> {
    <TComponent extends TBase, TBuilder extends ComponentBuilder> ComponentRegistrar<TBase> register(ComponentType<TBase, TComponent, TBuilder> componentType, UnaryOperator<TBuilder> builder);

    default ComponentRegistrar<TBase> register(ComponentType<TBase, ?, ?>... componentTypes) {
        for(var componentType : componentTypes) {
            register(componentType, UnaryOperator.identity());
        }

        return this;
    }
}
