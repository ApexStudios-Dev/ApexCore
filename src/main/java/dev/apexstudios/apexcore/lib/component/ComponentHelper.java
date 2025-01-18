package dev.apexstudios.apexcore.lib.component;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.ApiStatus;

public interface ComponentHelper {
    @ApiStatus.Internal
    static <TBase extends Component<TBase>, THolder extends ComponentHolder<TBase>> Map<ComponentType<TBase, ?, ?>, TBase> registerComponents(THolder holder, BiConsumer<THolder, ComponentRegistrar<TBase>> consumer) {
        var registrar = new ComponentRegistrar<TBase>() {
            private final Multimap<ComponentType<TBase, ?, ?>, UnaryOperator<? extends ComponentBuilder>> listeners = LinkedListMultimap.create();

            @Override
            public <TComponent extends TBase, TBuilder extends ComponentBuilder> ComponentRegistrar<TBase> register(ComponentType<TBase, TComponent, TBuilder> componentType, UnaryOperator<TBuilder> builder) {
                listeners.put(componentType, builder);
                return this;
            }
        };

        consumer.accept(holder, registrar);

        var map = Maps.<ComponentType<TBase, ?, ?>, TBase>newLinkedHashMap();

        for(var componentType : registrar.listeners.keySet()) {
            var component = componentType.newInstance(holder, builder -> registrar.listeners
                    .get(componentType)
                    .forEach(modifier -> ((UnaryOperator) modifier).apply(builder))
            );

            map.put(componentType, component);
        }

        return Collections.unmodifiableMap(map);
    }
}
