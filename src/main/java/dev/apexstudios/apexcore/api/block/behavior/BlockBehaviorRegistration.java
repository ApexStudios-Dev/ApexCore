package dev.apexstudios.apexcore.api.block.behavior;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class BlockBehaviorRegistration {
    final Map<Property<?>, Comparable<?>> properties = Maps.newHashMap();
    final IBehaviorBlock owner;

    BlockBehaviorRegistration(IBehaviorBlock owner) {
        this.owner = owner;
    }

    @CanIgnoreReturnValue
    public <TValue extends Comparable<TValue>, TProperty extends Property<TValue>> TProperty property(TProperty property, TValue defaultValue) {
        if(properties.putIfAbsent(property, defaultValue) != null) {
            throw new IllegalStateException("Duplicate BlockBehavior property registration: " + property.getName());
        }

        return property;
    }

    @CanIgnoreReturnValue
    public BooleanProperty booleanProperty(BooleanProperty property) {
        return property(property, false);
    }

    public BooleanProperty booleanProperty(String name, boolean defaultValue) {
        return property(BooleanProperty.create(name), defaultValue);
    }

    public BooleanProperty booleanProperty(String name) {
        return booleanProperty(name, false);
    }

    public <TValue extends Enum<TValue> & StringRepresentable> EnumProperty<TValue> enumProperty(String name, List<TValue> values, TValue defaultValue) {
        return property(EnumProperty.create(name, defaultValue.getDeclaringClass(), values), defaultValue);
    }

    public <TValue extends Enum<TValue> & StringRepresentable> EnumProperty<TValue> enumProperty(String name, Predicate<TValue> filter, TValue defaultValue) {
        return enumProperty(name, Stream.of(defaultValue.getDeclaringClass().getEnumConstants()).filter(filter).toList(), defaultValue);
    }

    public <TValue extends Enum<TValue> & StringRepresentable> EnumProperty<TValue> enumProperty(String name, TValue defaultValue) {
        return enumProperty(name, Predicates.alwaysTrue(), defaultValue);
    }

    public IntegerProperty integerProperty(String name, int min, int max, int defaultValue) {
        return property(IntegerProperty.create(name, min, max), defaultValue);
    }

    public IntegerProperty integerProperty(String name, int min, int max) {
        return integerProperty(name, min, max, min);
    }
}
