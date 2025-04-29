package dev.apexstudios.apexcore.lib.component;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import java.util.function.UnaryOperator;

public class ComponentRegistrar<
        TBase extends Component<TBase, TObj, THolder, TType>,
        TObj,
        THolder extends ComponentHolder<TBase, TObj, THolder, TType>,
        TType extends ComponentType<TBase, ? extends TBase, TObj, THolder, TType, ?>,
        TSelf extends ComponentRegistrar<TBase, TObj, THolder, TType, TSelf>
> {
    final Multimap<TType, UnaryOperator<? super Object>> listeners = LinkedListMultimap.create();

    public final <TComponent extends TBase, TBuilder> TSelf register(ComponentType<TBase, TComponent, TObj, THolder, TType, TBuilder> componentType, UnaryOperator<TBuilder> builder) {
        listeners.put((TType) componentType, obj -> builder.apply((TBuilder) obj));
        return (TSelf) this;
    }

    @SafeVarargs
    public final TSelf register(TType... componentTypes) {
        for(var componentType : componentTypes) {
            listeners.put(componentType, UnaryOperator.identity());
        }

        return (TSelf) this;
    }
}
