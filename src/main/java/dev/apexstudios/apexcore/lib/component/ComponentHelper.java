package dev.apexstudios.apexcore.lib.component;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.ScheduledForRemoval
public interface ComponentHelper {
    @ApiStatus.Internal
    static <TBase extends Component<TBase, TObj>, THolder extends ComponentHolder<TBase, TObj>, TObj> Map<ComponentType<TBase, ?, TObj, ?>, TBase> registerComponents(THolder holder, BiConsumer<THolder, ComponentRegistrar<TBase, TObj>> consumer) {
        var registrar = new ComponentRegistrar<TBase, TObj>() {
            private final Multimap<ComponentType<TBase, ?, TObj, ?>, UnaryOperator<? extends ComponentBuilder>> listeners = LinkedListMultimap.create();

            @Override
            public <TComponent extends TBase, TBuilder extends ComponentBuilder> ComponentRegistrar<TBase, TObj> register(ComponentType<TBase, TComponent, TObj, TBuilder> componentType, UnaryOperator<TBuilder> builder) {
                listeners.put(componentType, builder);
                return this;
            }
        };

        consumer.accept(holder, registrar);

        var map = Maps.<ComponentType<TBase, ?, TObj, ?>, TBase>newLinkedHashMap();

        for(var componentType : registrar.listeners.keySet()) {
            var component = componentType.newInstance(holder, builder -> registrar.listeners
                    .get(componentType)
                    .forEach(modifier -> ((UnaryOperator) modifier).apply(builder))
            );

            map.put(componentType, component);
        }

        return Collections.unmodifiableMap(map);
    }

    static <TValue extends Comparable<TValue>> void validateCompatibilities(Property<TValue> property, Iterable<? extends Property<TValue>> compatibilities) {
        var possibleValues = property.getPossibleValues();

        for(var compatibility : compatibilities) {
            if(!compatibility.getPossibleValues().containsAll(possibleValues)) {
                var names = possibleValues.stream().map(compatibility::getName).collect(Collectors.joining(",", "[", "]"));
                throw new IllegalStateException("Compatibility Property['" + compatibility.getName() + "'] does not support all possible values: " + names);
            }
        }
    }
}
