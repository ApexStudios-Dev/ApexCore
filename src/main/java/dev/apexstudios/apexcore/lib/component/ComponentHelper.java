package dev.apexstudios.apexcore.lib.component;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;

public interface ComponentHelper {
    static <
            TBase extends Component<TBase, TObj, THolder, TType>,
            TObj,
            THolder extends ComponentHolder<TBase, TObj, THolder, TType>,
            TType extends ComponentType<TBase, ? extends TBase, TObj, THolder, TType, ?>,
            TRegistrar extends ComponentRegistrar<TBase, TObj, THolder, TType, TRegistrar>
    > Map<TType, TBase> registerComponents(THolder holder, Supplier<TRegistrar> registrarFactory, Consumer<TRegistrar> registrarConsumer) {
        var registrar = registrarFactory.get();
        registrarConsumer.accept(registrar);
        var map = Maps.<TType, TBase>newLinkedHashMap();

        for(var componentType : registrar.listeners.keySet()) {
            var component = componentType.newInstance(holder, builder -> registrar.listeners
                    .get(componentType)
                    .forEach(modifier -> modifier.apply(builder))
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
