package dev.apexstudios.apexcore.api.block.behavior;

import com.google.common.base.Predicates;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class SimplePropertyBlockBehavior<TValue extends Comparable<TValue>, TProperty extends Property<TValue>> extends BlockBehavior {
    private final TProperty property;
    private final TValue defaultValue;

    protected SimplePropertyBlockBehavior(BlockBehaviorRegistration registration, TProperty property, TValue defaultValue) {
        super(registration);

        this.defaultValue = defaultValue;
        this.property = registration.property(property, defaultValue);
    }

    public final TValue defaultValue() {
        return defaultValue;
    }

    public final TProperty property() {
        return property;
    }

    public final TValue get(BlockState blockState) {
        return blockState.getValueOrElse(property(), defaultValue());
    }

    public final BlockState set(BlockState blockState, TValue value) {
        return blockState.trySetValue(property(), value);
    }

    @Override
    protected BlockState copyProperties(BlockState blockState, BlockState neighborBlockState) {
        return set(blockState, get(neighborBlockState));
    }

    public static <TValue extends Comparable<TValue>, TProperty extends Property<TValue>> BlockBehaviorType<SimplePropertyBlockBehavior<TValue, TProperty>> create(TProperty property, TValue defaultValue) {
        return new BlockBehaviorType<>(registration -> new SimplePropertyBlockBehavior<>(registration, property, defaultValue));
    }

    public static BlockBehaviorType<SimplePropertyBlockBehavior<Boolean, BooleanProperty>> createBoolean(BooleanProperty property) {
        return create(property, false);
    }

    public static BlockBehaviorType<SimplePropertyBlockBehavior<Boolean, BooleanProperty>> createBoolean(String name, boolean defaultValue) {
        return create(BooleanProperty.create(name), defaultValue);
    }

    public static BlockBehaviorType<SimplePropertyBlockBehavior<Boolean, BooleanProperty>> createBoolean(String name) {
        return createBoolean(name, false);
    }

    public static <TValue extends Enum<TValue> & StringRepresentable> BlockBehaviorType<SimplePropertyBlockBehavior<TValue, EnumProperty<TValue>>> createEnum(String name, List<TValue> values, TValue defaultValue) {
        return create(EnumProperty.create(name, defaultValue.getDeclaringClass(), values), defaultValue);
    }

    public static <TValue extends Enum<TValue> & StringRepresentable> BlockBehaviorType<SimplePropertyBlockBehavior<TValue, EnumProperty<TValue>>> createEnum(String name, Predicate<TValue> filter, TValue defaultValue) {
        return createEnum(name, Stream.of(defaultValue.getDeclaringClass().getEnumConstants()).filter(filter).toList(), defaultValue);
    }

    public static <TValue extends Enum<TValue> & StringRepresentable> BlockBehaviorType<SimplePropertyBlockBehavior<TValue, EnumProperty<TValue>>> createEnum(String name, TValue defaultValue) {
        return createEnum(name, Predicates.alwaysTrue(), defaultValue);
    }

    public static BlockBehaviorType<SimplePropertyBlockBehavior<Integer, IntegerProperty>> createInteger(String name, int min, int max, int defaultValue) {
        return create(IntegerProperty.create(name, min, max), defaultValue);
    }

    public static BlockBehaviorType<SimplePropertyBlockBehavior<Integer, IntegerProperty>> createInteger(String name, int min, int max) {
        return createInteger(name, min, max, min);
    }
}
